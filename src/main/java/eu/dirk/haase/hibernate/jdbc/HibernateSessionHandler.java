package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.jdbc.Work;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class HibernateSessionHandler extends AbstractHibernateSessionHandler<Session> {

    private static final ThreadLocal<Reference<HibernateSessionLinker>> threadLocalSession = new ThreadLocal<>();

    public HibernateSessionHandler(final Session delegate) {
        super(delegate);
    }

    public static Optional<HibernateSessionLinker> currentLinker() {
        final Reference<HibernateSessionLinker> linker = threadLocalSession.get();
        return Optional.ofNullable(linker != null ? linker.get() : null);
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
                    return (iface2.isInstance(this.delegate) ? this.delegate : null);
                }
            case "close":
                this.isClosed = true;
                becomeObsoleteLinker(this.linker);
                unlinkHibernate();
                return method.invoke(this.delegate, args);
            case "disconnect":
                becomeObsoleteLinker(this.linker);
                unlinkHibernate();
                return method.invoke(this.delegate, args);
            case "reconnect":
                becomeObsoleteLinker(this.linker);
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
                hibernateConnection.linkSession(this.hibernateReference, this.connectionReference);
            } catch (SQLException ex) {
                throw new HibernateException(ex.toString(), ex);
            }
        }
    }

}
