package eu.dirk.haase.hibernate.jdbc.lazy.supplier;

import javax.sql.CommonDataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

abstract class AbstractDataSourceSupplier<T extends CommonDataSource> implements DataSourceSupplier<T> {

    private final Set<Class<?>> apiInterfaceSet;
    private String description;
    private long initializedTimeMillis;
    private boolean isInitialized;
    private PrintWriter logWriter;
    private Integer loginTimeout;
    AbstractDataSourceSupplier(final Class<?>[] apiInterfaces) {
        this.apiInterfaceSet = buildApiSet(apiInterfaces);
    }

    private Set<Class<?>> buildApiSet(final Class<?>[] apiInterfaces) {
        final Set<Class<?>> apiSet = new HashSet<>();
        for (final Class<?> clazz : apiInterfaces) {
            apiSet.add(clazz);
        }
        return Collections.unmodifiableSet(apiSet);
    }

    @Override
    public final T get() {
        final T dataSource = getInternal();
        init(dataSource);
        if (!isInitialized) {
            this.initializedTimeMillis = System.currentTimeMillis();
        }
        isInitialized = true;
        return dataSource;
    }

    @Override
    public Set<Class<?>> getApiInterfaceSet() {
        return apiInterfaceSet;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final void setDescription(final String description) {
        if (description != null) {
            this.description = description;
        }
    }

    abstract T getInternal();

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public final void setLogWriter(final PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    @Override
    public int getLoginTimeout() {
        return (loginTimeout != null ? loginTimeout : 0);
    }

    @Override
    public final void setLoginTimeout(final int loginTimeoutSeconds) {
        this.loginTimeout = loginTimeout;
    }

    @Override
    public final long getTimeMillisOfInitialization() {
        return initializedTimeMillis;
    }

    private void init(final T dataSource) {
        try {
            if (this.logWriter != null) {
                dataSource.setLogWriter(this.logWriter);
            } else {
                this.logWriter = dataSource.getLogWriter();
            }
            if (this.loginTimeout != null) {
                dataSource.setLoginTimeout(this.loginTimeout);
            } else {
                this.loginTimeout = dataSource.getLoginTimeout();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
    }

    @Override
    public void invalidate() {
        isInitialized = false;
        initializedTimeMillis = -1;
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(" + System.identityHashCode(this) + "){" + getDescription() + "}";
    }

}
