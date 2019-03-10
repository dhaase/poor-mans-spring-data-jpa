package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import javax.persistence.EntityManager;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class HibernateEntityManagerHandler extends AbstractHibernateSessionHandler<EntityManager> {

    private final EntityManager delegate;

    public HibernateEntityManagerHandler(final EntityManager delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    void doWork(final EntityManager entityManager, final Work work) throws HibernateException {
        final Session session = getSession(entityManager);
        session.doWork(work);
    }

    private Session getSession(EntityManager entityManager) {
        return entityManager.unwrap(Session.class);
    }


    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        switch (method.getName()) {
            case "isWrapperFor":
                final Class<?> iface1 = (Class<?>) args[0];
                return iface1.isInstance(proxy);
            case "unwrap":
                final Class<?> iface2 = (Class<?>) args[0];
                return (iface2.isInstance(proxy) ? proxy : null);
            case "close":
                unlinkHibernate();
                return method.invoke(delegate, args);
            default:
                ensureLinkedHibernate();
                return method.invoke(delegate, args);
        }
    }

    @Override
    void linkHibernate(final Connection connection) {
        if (isHibernateConnection()) {
            try {
                final HibernateConnection hibernateConnection = connection.unwrap(HibernateConnection.class);
                this.connectionReference = new WeakReference<>(hibernateConnection);
                this.hibernateReference = new WeakReference<>(delegate);
                final Reference<Session> sessionReference = new WeakReference<>(getSession(this.delegate));
                hibernateConnection.linkEntityManager(this.hibernateReference, connectionReference);
                hibernateConnection.linkSession(sessionReference, connectionReference);
            } catch (SQLException ex) {
                throw new HibernateException(ex.toString(), ex);
            }
        }
    }

}
