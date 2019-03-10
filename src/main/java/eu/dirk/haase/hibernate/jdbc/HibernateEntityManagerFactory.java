package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.classic.Session;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.lang.reflect.Proxy;
import java.util.Map;

public class HibernateEntityManagerFactory implements EntityManagerFactory {

    private final EntityManagerFactory delegate;

    public HibernateEntityManagerFactory(final EntityManagerFactory delegate) {
        this.delegate = delegate;
    }

    private EntityManager proxyEntityManager(EntityManager session) {
        if ((session instanceof Wrapper) && ((Wrapper) session).isWrapperFor(HibernateEntityManager.class)) {
            return session;
        } else {
            final Class<?>[] interfaces = {Session.class, HibernateEntityManager.class};
            final ClassLoader loader = this.getClass().getClassLoader();
            return (EntityManager) Proxy.newProxyInstance(loader, interfaces, new HibernateEntityManagerHandler(session));
        }
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return proxyEntityManager(delegate.createEntityManager(map));
    }

    @Override
    public EntityManager createEntityManager() {
        return proxyEntityManager(delegate.createEntityManager());
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }
}
