package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.jdbc.Work;

import javax.persistence.EntityManager;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class HibernateEntityManagerHandler extends AbstractHibernateSessionHandler<EntityManager> {

    private static final ThreadLocal<Reference<HibernateSessionLinker>> threadLocalSession = new ThreadLocal<>();

    public HibernateEntityManagerHandler(final EntityManager delegate) {
        super(delegate);
    }

    public static Optional<HibernateSessionLinker> currentLinker() {
        final Reference<HibernateSessionLinker> linker = threadLocalSession.get();
        return Optional.ofNullable(linker != null ? linker.get() : null);
    }

    @Override
    void doWork(final EntityManager entityManager, final Work work) throws HibernateException {
        final Session session = entityManager.unwrap(Session.class);
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
                    final Object innerObject = method.invoke(this.delegate, args);
                    return (innerObject instanceof Session
                            ? HibernateSession.proxySession((Session) innerObject)
                            : innerObject);
                }
            case "close":
                becomeObsoleteLinker(this.linker);
                this.isClosed = true;
                unlinkHibernate();
                return method.invoke(this.delegate, args);
            default:
                becomeCurrentLinker(threadLocalSession);
                return method.invoke(this.delegate, args);
        }
    }

    @Override
    public void linkHibernate(final Connection connection) {
        if (this.isHibernateConnection && !this.isClosed) {
            try {
                final IHibernateConnection hibernateConnection = connection.unwrap(IHibernateConnection.class);
                this.connectionReference = new WeakReference<>(hibernateConnection);
                this.hibernateReference = new WeakReference<>(this.delegate);
                hibernateConnection.linkEntityManager(this.hibernateReference, connectionReference);
            } catch (SQLException ex) {
                throw new HibernateException(ex.toString(), ex);
            }
        }
    }

}
