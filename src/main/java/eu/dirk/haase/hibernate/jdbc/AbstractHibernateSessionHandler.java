package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.jdbc.Work;

import java.lang.ref.Reference;
import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;

abstract class AbstractHibernateSessionHandler<T> implements InvocationHandler {

    private final T delegate;
    Reference<Connection> connectionReference;
    Reference<T> hibernateReference;
    private boolean isHibernateConnection;

    AbstractHibernateSessionHandler(final T delegate) {
        this.delegate = delegate;
        doWork(this.delegate, (c) -> determineHibernateConnection(c));
        ensureLinkedHibernate();
    }

    private void determineHibernateConnection(final Connection connection) {
        try {
            isHibernateConnection = connection.isWrapperFor(HibernateConnection.class);
        } catch (SQLException ex) {
            throw new HibernateException(ex.toString(), ex);
        }
    }

    abstract void doWork(final T delegate, final Work work) throws HibernateException;

    void ensureLinkedHibernate() {
        if (isHibernateConnection) {
            if ((connectionReference == null) || (connectionReference.get() == null)) {
                doWork(this.delegate, (c) -> linkHibernate(c));
            }
        }
    }

    public boolean isHibernateConnection() {
        return isHibernateConnection;
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
