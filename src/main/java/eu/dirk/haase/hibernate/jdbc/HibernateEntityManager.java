package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.classic.Session;

import javax.persistence.EntityManager;
import java.lang.reflect.Proxy;

public interface HibernateEntityManager extends Wrapper {

    static EntityManager proxyEntityManager(final EntityManager entityManager) {
        if ((entityManager instanceof Wrapper) && ((Wrapper) entityManager).isWrapperFor(HibernateEntityManager.class)) {
            return entityManager;
        } else {
            final Class<?>[] interfaces = {Session.class, HibernateEntityManager.class};
            final ClassLoader loader = HibernateEntityManager.class.getClassLoader();
            return (EntityManager) Proxy.newProxyInstance(loader, interfaces, new HibernateEntityManagerHandler(entityManager));
        }
    }

}
