package eu.dirk.haase.hibernate.jdbc.lazy.internal;

import eu.dirk.haase.MemoizingSupplier;
import eu.dirk.haase.jdbc.lazy.supplier.DataSourceSupplier;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class CLazyHybridXADataSource
        <T1 extends DataSource & XADataSource> extends CLazyDataSource<T1> implements XADataSource {
    private static final Set<Class<?>> API_INTERFACES;

    static {
        Set<Class<?>> apis = new HashSet<>();
        apis.add(DataSource.class);
        apis.add(XADataSource.class);
        API_INTERFACES = Collections.unmodifiableSet(apis);
    }

    private final MemoizingSupplier<T1> delegateSupplier;

    public CLazyHybridXADataSource(final DataSourceSupplier<T1> delegateSupplier) {
        this(API_INTERFACES, delegateSupplier, new MemoizingSupplier<>(delegateSupplier));
    }

    private CLazyHybridXADataSource(final Set<Class<?>> apiInterfaceSet,
                                    final DataSourceSupplier<T1> dataSourceSupplier,
                                    final MemoizingSupplier<T1> memoizingSupplier) {
        super(apiInterfaceSet,dataSourceSupplier, memoizingSupplier);
        this.delegateSupplier = memoizingSupplier;
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return delegateSupplier.get().getXAConnection();
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return delegateSupplier.get().getXAConnection(user, password);
    }

}
