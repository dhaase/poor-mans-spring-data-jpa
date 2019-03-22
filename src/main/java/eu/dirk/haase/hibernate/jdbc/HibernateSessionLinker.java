package eu.dirk.haase.hibernate.jdbc;

import java.sql.Connection;

public interface HibernateSessionLinker {

    void linkHibernate(final Connection connection);

}
