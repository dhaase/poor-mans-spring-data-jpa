package eu.dirk.haase.hibernate.jdbc.lazy.supplier;

import javax.sql.XADataSource;

public final class JndiXADataSourceSupplier<T extends XADataSource> extends AbstractJndiDataSourceSupplier<T> {

    public JndiXADataSourceSupplier() {
        super(XADataSource.class);
    }

}
