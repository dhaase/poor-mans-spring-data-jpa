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

    AbstractHibernateSessionHandler(final T delegate) {
        this.delegate = delegate;
        doWork(this.delegate, (c) -> determineHibernateConnection(c));
        ensureLinkedHibernate();
    }

    private void determineHibernateConnection(final Connection connection) {
        try {
            isHibernateConnection = connection.isWrapperFor(HibernateConnection.class);
        } catch (SQLException ex) {
            // Manche Treiber loesen eine Exception aus
            // dabei ist die Frage doch leicht zu beantworten:
            isHibernateConnection = false;
        }
    }

    abstract void doWork(final T delegate, final Work work) throws HibernateException;

    void ensureLinkedHibernate() {
        if (isHibernateConnection) {
            final boolean isConnectionAbsent = (connectionReference == null) || (connectionReference.get() == null);
            final boolean isHibernateAbsent = (hibernateReference == null) || (hibernateReference.get() == null);
            if (isConnectionAbsent || isHibernateAbsent) {
                doWork(this.delegate, (c) -> linkHibernate(c));
            }
        }
    }

    abstract void linkHibernate(final Connection connection);

    void unlinkHibernate() {
        if (isHibernateConnection) {
            if (this.connectionReference != null) {
                this.connectionReference.clear();
            }
            if (this.hibernateReference != null) {
                this.hibernateReference.clear();
            }
        }
    }

}
