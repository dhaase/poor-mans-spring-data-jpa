package eu.dirk.haase.hibernate.jdbc.lazy.supplier;

import javax.sql.DataSource;
import javax.sql.XADataSource;

public final class JndiHybridXADataSourceSupplier
        <T extends DataSource & XADataSource> extends AbstractJndiDataSourceSupplier<T> {

    private static final Class[] PROXY_INTERFACES = {DataSource.class, XADataSource.class};

    public JndiHybridXADataSourceSupplier() {
        super(PROXY_INTERFACES);
    }

}
