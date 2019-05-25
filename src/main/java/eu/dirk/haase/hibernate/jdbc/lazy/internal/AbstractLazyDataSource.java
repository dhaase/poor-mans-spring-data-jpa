package eu.dirk.haase.hibernate.jdbc.lazy.internal;

import eu.dirk.haase.MemoizingSupplier;
import eu.dirk.haase.hibernate.jdbc.lazy.LazyDataSource;
import eu.dirk.haase.hibernate.jdbc.lazy.supplier.DataSourceSupplier;
import eu.dirk.haase.jdbc.lazy.LazyDataSource;
import eu.dirk.haase.jdbc.lazy.supplier.DataSourceSupplier;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.CommonDataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Set;
import java.util.logging.Logger;

class AbstractLazyDataSource<T1 extends CommonDataSource> implements CommonDataSource, LazyDataSource, InitializingBean {

    private final DataSourceSupplier<T1> dataSourceSupplier;
    private final MemoizingSupplier<T1> memoizingSupplier;
    private final long startupTimeMillis;
    private boolean isLazyInit;

    AbstractLazyDataSource(final Set<Class<?>> apiInterfaceSet,
                           final DataSourceSupplier<T1> dataSourceSupplier,
                           final MemoizingSupplier<T1> memoizingSupplier) {
        this.isLazyInit = true;
        this.memoizingSupplier = memoizingSupplier;
        this.dataSourceSupplier = checkDataSourceSupplier(apiInterfaceSet, dataSourceSupplier);
        this.startupTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!isLazyInit) {
            // Initialisiere das innere Objekt
            // und mache den ersten API-Call um
            // eventuelle andere Lazy-Mechanismen
            // auszuhebeln.
            // getLoginTimeout() ist ein API-Call der
            // die DataSource nicht veraendert:
            memoizingSupplier.get().getLoginTimeout();
        }
    }

    private DataSourceSupplier<T1> checkDataSourceSupplier(final Set<Class<?>> apiInterfaceSet,
                                                           final DataSourceSupplier<T1> dataSourceSupplier) {
        // Ja, ja, so ist es: Sobald man Generics einsetzt
        // ist es vorbei mit der (statischen) Typsicherheit.
        // Daher erfolgt hier noch eine Ueberpruefung zur
        // Laufzeit:
        if (dataSourceSupplier.getApiInterfaceSet().containsAll(apiInterfaceSet)) {
            return dataSourceSupplier;
        } else {
            throw new IllegalArgumentException("Supplier does not return an object that is a Subclass of " + apiInterfaceSet);
        }
    }

    @Override
    public final PrintWriter getLogWriter() throws SQLException {
        if (memoizingSupplier.isPresent()) {
            return memoizingSupplier.get().getLogWriter();
        } else {
            return dataSourceSupplier.getLogWriter();
        }
    }

    @Override
    public final void setLogWriter(PrintWriter out) throws SQLException {
        if (memoizingSupplier.isPresent()) {
            memoizingSupplier.get().setLogWriter(out);
        } else {
            dataSourceSupplier.setLogWriter(out);
        }
    }

    @Override
    public final int getLoginTimeout() throws SQLException {
        if (memoizingSupplier.isPresent()) {
            return memoizingSupplier.get().getLoginTimeout();
        } else {
            return dataSourceSupplier.getLoginTimeout();
        }
    }

    @Override
    public final void setLoginTimeout(int seconds) throws SQLException {
        if (memoizingSupplier.isPresent()) {
            memoizingSupplier.get().setLoginTimeout(seconds);
        } else {
            dataSourceSupplier.setLoginTimeout(seconds);
        }
    }

    @Override
    public final Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return memoizingSupplier.get().getParentLogger();
    }

    @Override
    public final long getTimeMillisOfInitialization() {
        return dataSourceSupplier.getTimeMillisOfInitialization();
    }

    @Override
    public final long getTimeMillisOfStartup() {
        return startupTimeMillis;
    }

    @Override
    public int identityHashCode() {
        if (memoizingSupplier.isPresent()) {
            return System.identityHashCode(memoizingSupplier.get());
        } else {
            return 0;
        }
    }

    @Override
    public final void invalidate() {
        dataSourceSupplier.invalidate();
        memoizingSupplier.invalidate();
    }

    @Override
    public boolean isLazyInit() {
        return isLazyInit;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        isLazyInit = lazyInit;
    }

    @Override
    public final boolean isPresent() {
        return memoizingSupplier.isPresent();
    }

    @Override
    public final String toString() {
        return memoizingSupplier.isPresent()
                ? memoizingSupplier.get().toString()
                : "LazyDataSource(" + System.identityHashCode(this) + "){" + dataSourceSupplier.getDescription() + "}";
    }


}
