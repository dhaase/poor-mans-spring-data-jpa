package eu.dirk.haase.hibernate;

import eu.dirk.haase.hibernate.jdbc.HibernateConnection;
import eu.dirk.haase.hibernate.jdbc.HibernateDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class H2ConnectionProvider implements ConnectionProvider {

    private DataSource dataSource;

    public H2ConnectionProvider() {
    }

    @Override
    public void configure(Properties properties) throws HibernateException {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");
        this.dataSource = new HibernateDataSource(ds);
    }

    @Override
    public Connection getConnection() throws SQLException {
        final HibernateConnection connection = (HibernateConnection) dataSource.getConnection();
        return connection;
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public void close() throws HibernateException {

    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }
}
