package eu.dirk.haase.hibernate.jdbc.lazy.supplier;


import oracle.jdbc.xa.client.OracleXADataSource;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.Properties;

public final class OracleDataSourceSupplier<T extends DataSource & XADataSource & ConnectionPoolDataSource> extends AbstractDataSourceSupplier<T> {

    private static final Class[] API_INTERFACES = {DataSource.class, XADataSource.class, ConnectionPoolDataSource.class};

    private String url;
    private String user;
    private String password;
    private Properties properties;

    public OracleDataSourceSupplier() {
        super(API_INTERFACES);
    }

    @Override
    T getInternal() {
        try {
            final OracleXADataSource oracleXADataSource = new OracleXADataSource();
            oracleXADataSource.setURL(url);
            oracleXADataSource.setUser(user);
            oracleXADataSource.setPassword(password);
            return (T) oracleXADataSource;
        } catch (SQLException ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUrl(final String url) {
        setDescription(url);
        this.url = url;
    }

    public void setUser(final String user) {
        this.user = user;
    }

}
