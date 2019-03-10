package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.classic.Session;

import java.lang.reflect.Proxy;

public interface HibernateSession extends Wrapper {

    static Session proxySession(final Session session) {
        if ((session instanceof Wrapper) && ((Wrapper) session).isWrapperFor(HibernateSession.class)) {
            return session;
        } else {
            final Class<?>[] interfaces = {Session.class, HibernateSession.class};
            final ClassLoader loader = HibernateSession.class.getClassLoader();
            return (Session) Proxy.newProxyInstance(loader, interfaces, new HibernateSessionHandler(session));
        }
    }

}
