package eu.dirk.haase.hibernate.jdbc.lazy.supplier;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

public final class H2DataSourceSupplier<T extends DataSource & XADataSource & ConnectionPoolDataSource> extends AbstractDataSourceSupplier<T> {
    private static final Class[] API_INTERFACES = {DataSource.class, XADataSource.class, ConnectionPoolDataSource.class};
    private String password;
    private String url;
    private String user;

    public H2DataSourceSupplier() {
        super(API_INTERFACES);
    }

    @SuppressWarnings("unchecked")
    @Override
    T getInternal() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl(url);
        jdbcDataSource.setUser(user);
        jdbcDataSource.setPassword(password);
        return (T) jdbcDataSource;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUrl(final String url) {
        if (getDescription() == null) {
            setDescription(url);
        }
        this.url = url;
    }

    public void setUser(final String user) {
        this.user = user;
    }

}
