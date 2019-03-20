package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.jdbc.Work;

import java.lang.ref.Reference;
import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;

abstract class AbstractHibernateSessionHandler<T> implements InvocationHandler {

    final T delegate;
    Reference<IHibernateConnection> connectionReference;
    Reference<T> hibernateReference;
    boolean isHibernateConnection;
    boolean isClosed;

    AbstractHibernateSessionHandler(final T delegate) {
        this.isClosed = false;
        this.delegate = delegate;
        doWork(this.delegate, (c) -> determineHibernateConnection(c));
        ensureLinkedHibernate();
    }

    private void determineHibernateConnection(final Connection connection) {
        try {
            this.isHibernateConnection = connection.isWrapperFor(HibernateConnection.class);
        } catch (SQLException ex) {
            // Manche Treiber loesen eine Exception aus
            // dabei ist die Frage doch leicht zu beantworten:
            this.isHibernateConnection = false;
        }
    }

    abstract void doWork(final T delegate, final Work work) throws HibernateException;

    void ensureLinkedHibernate() {
        if (this.isHibernateConnection && !this.isClosed) {
            final boolean isConnectionAbsent = (this.connectionReference == null) || (this.connectionReference.get() == null);
            final boolean isHibernateAbsent = (this.hibernateReference == null) || (this.hibernateReference.get() == null);
            if (isConnectionAbsent || isHibernateAbsent) {
                doWork(this.delegate, (c) -> linkHibernate(c));
            }
        }
    }

    abstract void linkHibernate(final Connection connection);

    void unlinkHibernate() {
        if (this.isHibernateConnection) {
            if (this.connectionReference != null) {
                this.connectionReference.clear();
            }
            if (this.hibernateReference != null) {
                this.hibernateReference.clear();
            }
        }
    }

}
