package eu.dirk.haase.hibernate.jdbc.lazy.supplier;

import javax.sql.DataSource;

public final class JndiDataSourceSupplier<T extends DataSource> extends AbstractJndiDataSourceSupplier<T> {

    public JndiDataSourceSupplier() {
        super(DataSource.class);
    }

}
