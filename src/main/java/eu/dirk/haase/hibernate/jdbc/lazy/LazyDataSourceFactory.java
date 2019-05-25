package eu.dirk.haase.hibernate.jdbc.lazy;

import eu.dirk.haase.jdbc.lazy.internal.CLazyDataSource;
import eu.dirk.haase.jdbc.lazy.internal.CLazyHybridXADataSource;
import eu.dirk.haase.jdbc.lazy.internal.CLazyXADataSource;
import eu.dirk.haase.jdbc.lazy.supplier.DataSourceSupplier;

import javax.sql.DataSource;
import javax.sql.XADataSource;

public final class LazyDataSourceFactory {

    @SuppressWarnings("unchecked")
    public static <T extends DataSource & LazyDataSource>
    T lazyDataSource(final DataSourceSupplier<T> dataSourceSupplier) {
        return (T) new CLazyDataSource<>(dataSourceSupplier);
    }

    @SuppressWarnings("unchecked")
    public static <T extends XADataSource & LazyDataSource>
    T lazyXADataSource(final DataSourceSupplier<T> dataSourceSupplier) {
        return (T) new CLazyXADataSource<>(dataSourceSupplier);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DataSource & XADataSource & LazyDataSource>
    T lazyHybridXADataSource(final DataSourceSupplier<T> dataSourceSupplier) {
        return (T) new CLazyHybridXADataSource<>(dataSourceSupplier);
    }

    private LazyDataSourceFactory() {
    }
}
