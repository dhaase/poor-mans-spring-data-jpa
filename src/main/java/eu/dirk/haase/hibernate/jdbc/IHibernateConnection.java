package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.classic.Session;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.lang.ref.Reference;
import java.sql.Connection;

/**
 * Eine {@link Connection} die dieses Interface implementiert wird intern
 * von einer Hibernate-Session oder einem Hibernate-EntityManager verwendet.
 * <p>
 * Diese {@link Connection} geht von der Pr&auml;misse aus, das es zur einer
 * bestimmten Zeit stets h&ouml;chstens nur eine Hibernate-Session
 * oder / und einen Hibernate-EntityManager geben kann.
 * <p>
 * Daraus ergibt sich implizit das es zur einer bestimmten {@link DataSource}
 * (die alle {@link Connection}s erzeugt) stets h&ouml;chstens nur eine
 * Hibernate-SessionFactory oder / und eine Hibernate-EntityManagerFactory
 * gibt.
 * <p>
 * Diese Beschr&auml;nkung besteht aus Vereinfachungsgr&uuml;nden, es gibt
 * keine harten technischen Gr&uuml;nde daf&uuml;r.
 */
public interface IHibernateConnection {

    /**
     * F&uuml;hrt ein {@code flush} an den dieser {@link Connection} registierten
     * Hibernate-Session und Hibernate-EntityManager aus.
     *
     * @see org.hibernate.Session#flush()
     * @see EntityManager#flush()
     */
    void flush();

    /**
     * Verbindet diese {@link Connection} mit einem Hibernate-EntityManager.
     *
     * @param entityManagerReference die Hibernate-EntityManager Reference mit der diese
     *                               {@link Connection} verbunden werden soll.
     * @param connectionReference    eine Reference auf diese {@link Connection}.
     */
    void linkEntityManager(Reference<EntityManager> entityManagerReference, Reference<IHibernateConnection> connectionReference);

    /**
     * Verbindet diese {@link Connection} mit einem Hibernate-Session.
     *
     * @param sessionReference    die Hibernate-Session Reference mit der diese
     *                            {@link Connection} verbunden werden soll.
     * @param connectionReference eine Reference auf diese {@link Connection}.
     */
    void linkSession(Reference<Session> sessionReference, Reference<IHibernateConnection> connectionReference);

    /**
     * Liefert {@code true} wenn diese {@link Connection}
     * mit einer Hibernate-Session verbunden ist.
     *
     * @return {@code true} wenn diese {@link Connection}
     * mit einer Hibernate-Session verbunden ist.
     */
    boolean isSessionLinked();

    /**
     * Liefert {@code true} wenn diese {@link Connection}
     * mit einer Hibernate-EntityManager verbunden ist.
     *
     * @return {@code true} wenn diese {@link Connection}
     * mit einer Hibernate-EntityManager verbunden ist.
     */
    boolean isEntityManagerLinked();
}
