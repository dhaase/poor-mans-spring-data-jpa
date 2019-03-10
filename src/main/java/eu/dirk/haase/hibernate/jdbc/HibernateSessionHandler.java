package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class HibernateSessionHandler implements InvocationHandler {

    private final Session delegate;
    private Reference<Connection> connectionReference;
    private Reference<Session> sessionReference;

    public HibernateSessionHandler(final Session delegate) {
        this.delegate = delegate;
    }

    private void ensureLinkedSession() {
        if ((connectionReference == null) || (connectionReference.get() == null)) {
            linkSession(delegate.connection());
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "isWrapperFor":
                final Class<?> iface1 = (Class<?>) args[0];
                return iface1.isInstance(proxy);
            case "unwrap":
                final Class<?> iface2 = (Class<?>) args[0];
                return (iface2.isInstance(proxy) ? proxy : null);
            case "connection":
                return method.invoke(delegate, args);
            case "close":
                unlinkSession();
                return method.invoke(delegate, args);
            case "disconnect":
                unlinkSession();
                return method.invoke(delegate, args);
            case "reconnect":
                unlinkSession();
                return method.invoke(delegate, args);
            default:
                ensureLinkedSession();
                return method.invoke(delegate, args);
        }
    }

    private void unlinkSession() {
        connectionReference.clear();
        sessionReference.clear();
    }

    private void linkSession(Connection connection) {
        try {
            final HibernateConnection hibernateConnection = connection.unwrap(HibernateConnection.class);
            this.connectionReference = new WeakReference<>(hibernateConnection);
            this.sessionReference = new WeakReference<>(delegate);
            hibernateConnection.setSessionReference(this.sessionReference, connectionReference);
        } catch (SQLException ex) {
            throw new HibernateException(ex.toString(), ex);
        }
    }

}
