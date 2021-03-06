package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.classic.Session;

import javax.persistence.EntityManager;
import java.lang.ref.Reference;
import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;

public class HibernateConnection implements Connection, IHibernateConnection {

    private final Connection delegate;
    private HibernateReference<EntityManager> entityManagerReference;
    private HibernateReference<Session> sessionReference;

    public HibernateConnection(final Connection delegate) {
        this.delegate = delegate;
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        unlink();
        delegate.abort(executor);
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        System.out.println("++++++++++++++++++++++++++++++ close ++++++++++++++++++++++++++++++++++");
        unlink();
        delegate.close();
    }

    @Override
    public void commit() throws SQLException {
        delegate.commit();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    @Override
    public Statement createStatement() throws SQLException {
        ensureLinkedHibernate();
        return delegate.createStatement();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        ensureLinkedHibernate();
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        ensureLinkedHibernate();
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }

    private void ensureLinkedEntityManager() {
        final Optional<HibernateSessionLinker> linker = HibernateEntityManagerHandler.currentLinker();
        if (linker.isPresent()) {
            linker.get().linkHibernate(this);
        }
    }

    private void ensureLinkedHibernate() {
        ensureLinkedSession();
        ensureLinkedEntityManager();
    }

    private void ensureLinkedSession() {
        final Optional<HibernateSessionLinker> linker = HibernateSessionHandler.currentLinker();
        if (linker.isPresent()) {
            linker.get().linkHibernate(this);
        }
    }

    @Override
    public void flush() {
        if (this.sessionReference != null) {
            this.sessionReference.flush();
        }
        if (this.entityManagerReference != null) {
            this.entityManagerReference.flush();
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }

    @Override
    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public boolean isEntityManagerLinked() {
        return (this.entityManagerReference != null ? this.entityManagerReference.isLinked() : false);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }

    public boolean isSessionLinked() {
        return (this.sessionReference != null ? this.sessionReference.isLinked() : false);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return true;
        }
        return delegate.isWrapperFor(iface);
    }

    @Override
    public void linkEntityManager(final Reference<EntityManager> entityManagerReference, final Reference<IHibernateConnection> connectionReference) {
        if (this.entityManagerReference == null) {
            this.entityManagerReference = new HibernateReference<>(entityManagerReference, connectionReference);
        } else if (connectionReference.get() == this) {
            this.entityManagerReference = this.entityManagerReference.exchange(entityManagerReference, connectionReference);
        } else {
            // Diese Situation kann eigentlich nicht auftreten,
            // aber unter dem Tisch fallen moechte ich nun auch nicht.
            // Manchmal kann sich ja die urspruenglich gedachte Situation aendern:
            throw new IllegalStateException("This connection is NOT identical with the connection of the Hibernate EntityManager.");
        }
    }

    @Override
    public void linkSession(final Reference<Session> sessionReference, final Reference<IHibernateConnection> connectionReference) {
        if (this.sessionReference == null) {
            this.sessionReference = new HibernateReference<>(sessionReference, connectionReference);
        } else if (connectionReference.get() == this) {
            this.sessionReference = this.sessionReference.exchange(sessionReference, connectionReference);
        } else {
            // Diese Situation kann eigentlich nicht auftreten,
            // aber unter dem Tisch fallen moechte ich nun auch nicht.
            // Manchmal kann sich ja die urspruenglich gedachte Situation aendern:
            throw new IllegalStateException("This connection is NOT identical with the connection of the Hibernate Session.");
        }
        System.out.println(this.sessionReference);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        ensureLinkedHibernate();
        return delegate.prepareStatement(sql, columnNames);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public void rollback() throws SQLException {
        delegate.rollback();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        delegate.rollback(savepoint);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return delegate.setSavepoint(name);
    }

    private void unlink() {
        if (this.sessionReference != null) {
            this.sessionReference.unlink();
        }
        if (this.entityManagerReference != null) {
            this.entityManagerReference.unlink();
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return delegate.unwrap(iface);
    }

    static class HibernateReference<T> {
        final Reference<IHibernateConnection> connectionReference;
        final Reference<T> hibernateReference;

        HibernateReference(final Reference<T> hibernateReference, final Reference<IHibernateConnection> connectionReference) {
            Objects.nonNull(hibernateReference);
            Objects.nonNull(connectionReference);
            this.hibernateReference = hibernateReference;
            this.connectionReference = connectionReference;
        }

        HibernateReference<T> exchange(final Reference<T> nextHibernateReference, final Reference<IHibernateConnection> nextConnectionReference) {
            final boolean isSameHibernate = this.hibernateReference.get() == nextHibernateReference.get();
            final boolean isSameConnection = this.connectionReference.get() == nextConnectionReference.get();
            if (isSameHibernate && isSameConnection) {
                return this;
            } else {
                flush();
                unlink();
                return new HibernateReference(nextHibernateReference, nextConnectionReference);
            }
        }

        void flush() {
            final T hibernate = hibernateReference.get();
            if (hibernate instanceof Session) {
                ((Session) hibernate).flush();
            } else if (hibernate instanceof EntityManager) {
                ((EntityManager) hibernate).flush();
            }
        }

        boolean isLinked() {
            return hibernateReference.get() != null;
        }

        @Override
        public String toString() {
            return "HibernateReference{" +
                    "isLinked=" + isLinked() +
                    '}';
        }

        void unlink() {
            connectionReference.clear();
            hibernateReference.clear();
        }
    }

}
