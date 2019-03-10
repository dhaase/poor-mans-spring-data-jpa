package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.*;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

public class HibernateSessionFactory implements SessionFactory {

    private final SessionFactory delegate;

    public HibernateSessionFactory(final SessionFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() throws HibernateException {
        delegate.close();
    }

    @Override
    public boolean containsFetchProfileDefinition(String s) {
        return delegate.containsFetchProfileDefinition(s);
    }

    @Override
    public void evict(Class persistentClass) throws HibernateException {
        delegate.evict(persistentClass);
    }

    @Override
    public void evict(Class persistentClass, Serializable id) throws HibernateException {
        delegate.evict(persistentClass, id);
    }

    @Override
    public void evictCollection(String roleName) throws HibernateException {
        delegate.evictCollection(roleName);
    }

    @Override
    public void evictCollection(String roleName, Serializable id) throws HibernateException {
        delegate.evictCollection(roleName, id);
    }

    @Override
    public void evictEntity(String entityName) throws HibernateException {
        delegate.evictEntity(entityName);
    }

    @Override
    public void evictEntity(String entityName, Serializable id) throws HibernateException {
        delegate.evictEntity(entityName, id);
    }

    @Override
    public void evictQueries() throws HibernateException {
        delegate.evictQueries();
    }

    @Override
    public void evictQueries(String cacheRegion) throws HibernateException {
        delegate.evictQueries(cacheRegion);
    }

    @Override
    public Map getAllClassMetadata() throws HibernateException {
        return delegate.getAllClassMetadata();
    }

    @Override
    public Map getAllCollectionMetadata() throws HibernateException {
        return delegate.getAllCollectionMetadata();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
        return delegate.getClassMetadata(persistentClass);
    }

    @Override
    public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
        return delegate.getClassMetadata(entityName);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
        return delegate.getCollectionMetadata(roleName);
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        final Session session = delegate.getCurrentSession();
        return HibernateSession.proxySession(session);
    }

    @Override
    public Set getDefinedFilterNames() {
        return delegate.getDefinedFilterNames();
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return delegate.getFilterDefinition(filterName);
    }

    @Override
    public Reference getReference() throws NamingException {
        return delegate.getReference();
    }

    @Override
    public Statistics getStatistics() {
        return delegate.getStatistics();
    }

    @Override
    public TypeHelper getTypeHelper() {
        return delegate.getTypeHelper();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public Session openSession(Connection connection) {
        final Session session = delegate.openSession(connection);
        return HibernateSession.proxySession(session);
    }

    @Override
    public Session openSession(Interceptor interceptor) throws HibernateException {
        final Session session = delegate.openSession(interceptor);
        return HibernateSession.proxySession(session);
    }

    @Override
    public Session openSession(Connection connection, Interceptor interceptor) {
        final Session session = delegate.openSession(connection, interceptor);
        return HibernateSession.proxySession(session);
    }

    @Override
    public Session openSession() throws HibernateException {
        final Session session = delegate.openSession();
        return HibernateSession.proxySession(session);
    }

    @Override
    public StatelessSession openStatelessSession() {
        return delegate.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
        return delegate.openStatelessSession(connection);
    }


}
