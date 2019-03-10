package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.jdbc.Work;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class HibernateSessionHandler extends AbstractHibernateSessionHandler<Session> {


    public HibernateSessionHandler(final Session delegate) {
        super(delegate);
    }

    @Override
    void doWork(final Session session, Work work) throws HibernateException {
        session.doWork(work);
    }


    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        switch (method.getName()) {
            case "isWrapperFor":
                final Class<?> iface1 = (Class<?>) args[0];
                return iface1.isInstance(proxy);
            case "unwrap":
                final Class<?> iface2 = (Class<?>) args[0];
                if (iface2.isInstance(proxy)) {
                    return proxy;
                } else {
                    return (iface2.isInstance(delegate) ? delegate : null);
                }
            case "connection":
                return method.invoke(delegate, args);
            case "close":
                unlinkHibernate();
                return method.invoke(delegate, args);
            case "disconnect":
                unlinkHibernate();
                return method.invoke(delegate, args);
            case "reconnect":
                unlinkHibernate();
                return method.invoke(delegate, args);
            default:
                ensureLinkedHibernate();
                return method.invoke(delegate, args);
        }
    }

    @Override
    void linkHibernate(final Connection connection) {
        if (isHibernateConnection) {
            try {
                final HibernateConnection hibernateConnection = connection.unwrap(HibernateConnection.class);
                this.connectionReference = new WeakReference<>(hibernateConnection);
                this.hibernateReference = new WeakReference<>(delegate);
                hibernateConnection.linkSession(this.hibernateReference, connectionReference);
            } catch (SQLException ex) {
                throw new HibernateException(ex.toString(), ex);
            }
        }
    }

}
