package eu.dirk.haase.hibernate.jdbc.lazy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Main {

    public static void main(String... args) throws SQLException {
        ApplicationContext ac = new ClassPathXmlApplicationContext("application-context.xml");
        DataSource ds = ac.getBean("lazyDataSource", DataSource.class);
        System.out.println(ds);
        ds.getConnection();
        System.out.println(ds);
        System.out.println(new Timestamp(((LazyDataSource)ds).getTimeMillisOfStartup()));
        System.out.println(new Timestamp(((LazyDataSource)ds).getTimeMillisOfInitialization()));
        System.out.println(((LazyDataSource)ds).identityHashCode());
        ((LazyDataSource)ds).invalidate();
        ds.getConnection();
        System.out.println(new Timestamp(((LazyDataSource)ds).getTimeMillisOfStartup()));
        System.out.println(new Timestamp(((LazyDataSource)ds).getTimeMillisOfInitialization()));
        System.out.println(((LazyDataSource)ds).identityHashCode());

    }

}
