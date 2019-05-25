package eu.dirk.haase.hibernate.jdbc.lazy.internal;

import eu.dirk.haase.hibernate.jdbc.lazy.MemoizingSupplier;
import eu.dirk.haase.hibernate.jdbc.lazy.supplier.DataSourceSupplier;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CLazyDataSource<T1 extends DataSource> extends AbstractLazyDataSource<T1> implements DataSource {
    private static final Set<Class<?>> API_INTERFACES;

    static {
        Set<Class<?>> apis = new HashSet<>();
        apis.add(DataSource.class);
        API_INTERFACES = Collections.unmodifiableSet(apis);
    }

    private final MemoizingSupplier<T1> delegateSupplier;

    public CLazyDataSource(final DataSourceSupplier<T1> delegateSupplier) {
        this(API_INTERFACES, delegateSupplier, new MemoizingSupplier<>(delegateSupplier));
    }

    CLazyDataSource(final Set<Class<?>> apiInterfaceSet,
                    final DataSourceSupplier<T1> dataSourceSupplier,
                    final MemoizingSupplier<T1> memoizingSupplier) {
        super(apiInterfaceSet, dataSourceSupplier, memoizingSupplier);
        this.delegateSupplier = memoizingSupplier;
    }

    @Override
    public final Connection getConnection() throws SQLException {
        return delegateSupplier.get().getConnection();
    }

    @Override
    public final Connection getConnection(String username, String password) throws SQLException {
        return delegateSupplier.get().getConnection(username, password);
    }

    @Override
    public final boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegateSupplier.get().isWrapperFor(iface);
    }

    @Override
    public final <T2> T2 unwrap(Class<T2> iface) throws SQLException {
        return delegateSupplier.get().unwrap(iface);
    }


}
