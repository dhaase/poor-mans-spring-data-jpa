package eu.dirk.haase.hibernate.jdbc.lazy;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Wird von DataSources implementiert die erst beim ersten
 * API-Zugriff erzeugt und initialisiert werden soll.
 * <p>
 * Nur Zugriffe auf die API der Interfaces {@link DataSource}
 * und {@link XADataSource} f&uuml;hren dazu das das interne
 * DataSource-Objekt erzeugt und initialisiert wird.
 * <p>
 * Zugriffe auf die API von {@link Object} f&uuml;hren hingegen
 * nicht dazu das das interne DataSource erzeugt und initialisiert
 * wird.
 * <p>
 * Die Methoden {@link CommonDataSource#setLoginTimeout(int)} und
 * {@link CommonDataSource#setLogWriter(PrintWriter)} sowie deren
 * Getter-Methode werden lazy verarbeitet und f&uuml;hren daher
 * nicht dazu das das interne DataSource erzeugt und initialisiert
 * wird.
 *
 * @see DataSource
 * @see XADataSource
 */
public interface LazyDataSource {

    /**
     * Liefert eine Beschreibung dieser DataSource.
     *
     * @return eine Beschreibung dieser DataSource.
     */
    String getDescription();

    /**
     * Liefert den Protokollschreiber f&uuml;r das DataSource-Objekt
     * (das von internen Supplier geliefert wird) zur&uuml;ck.
     * <p>
     * Diese Methode wird lazy verarbeitet und f&uuml;hrt daher
     * nicht dazu das das interne DataSource erzeugt und initialisiert
     * wird.
     *
     * @return den Protokollschreiber f&uuml;r das DataSource-Objekt.
     * @see CommonDataSource#getLogWriter()
     */
    PrintWriter getLogWriter() throws SQLException;

    /**
     * Legt den Protokollschreiber f&uuml;r das DataSource-Objekt
     * das der interne Supplier liefert auf das angegebene
     * {@link PrintWriter}-Objekt fest.
     * <p>
     * Der {@link PrintWriter} ist ein Zeichenausgabestrom, auf den
     * alle Protokollierungs- und Verfolgungsnachrichten f&uuml;r diese
     * Datenquelle gedruckt werden.
     * <p>
     * Diese Methode wird lazy verarbeitet und f&uuml;hrt daher
     * nicht dazu das das interne DataSource erzeugt und initialisiert
     * wird.
     *
     * @param logWriter der neue Protokollschreiber; Zum Deaktivieren
     *                  der Protokollierung auf {@code null} setzen
     * @throws SQLException wenn ein Datenbankzugriffsfehler auftritt.
     * @see CommonDataSource#setLogWriter(PrintWriter)
     * @see DataSource#setLogWriter(PrintWriter)
     */
    void setLogWriter(final PrintWriter logWriter) throws SQLException;

    /**
     * Liefert die maximale Zeit in Sekunden, auf das das DataSource-Objekt
     * (das von internen Supplier geliefert wird) wartet, w&auml;hrend sie versucht,
     * eine Verbindung zu einer Datenbank herzustellen.
     * <p>
     * Diese Methode wird lazy verarbeitet und f&uuml;hrt daher
     * nicht dazu das das interne DataSource erzeugt und initialisiert
     * wird.
     *
     * @return die maximale Zeit in Sekunden f&uuml;r den Login.
     * @see CommonDataSource#getLoginTimeout()
     */
    int getLoginTimeout() throws SQLException;

    /**
     * Legt die maximale Zeit in Sekunden fest, auf das das DataSource-Objekt
     * (das dieser Supplier liefert) wartet, w&auml;hrend sie versucht,
     * eine Verbindung zu einer Datenbank herzustellen.
     * <p>
     * Ein Wert von Null gibt an, dass das Zeitlimit das Standard-Systemzeitlimit
     * ist, falls es eins gibt.
     * <p>
     * Diese Methode wird lazy verarbeitet und f&uuml;hrt daher
     * nicht dazu das das interne DataSource erzeugt und initialisiert
     * wird.
     *
     * @param loginTimeoutSeconds das Zeitlimit für die Datenquellenanmeldung.
     * @throws SQLException wenn ein Datenbankzugriffsfehler auftritt.
     * @see CommonDataSource#setLoginTimeout(int)
     * @see DataSource#setLoginTimeout(int)
     */
    void setLoginTimeout(int loginTimeoutSeconds) throws SQLException;

    /**
     * Liefert den Zeitpunkt der Initialisierung in Milli-Sekunden.
     * <p>
     * Der Zeitpunkt der Initialisierung liegt zeitlich stets nach
     * dem Start-Zeitpunkt, kann aber aufgrund der Granularit&auml;t
     * der JVM auf einen Zeitpunkt zusammenfallen.
     * <p>
     * Gemessen wird die Differenz in Millisekunden zwischen den Zeitpunkt
     * der Initialisierung und Mitternacht am 1. Januar 1970 UTC.
     *
     * @return den Zeitpunkt der Initialisierung in Milli-Sekunden.
     * @see System#currentTimeMillis()
     */
    long getTimeMillisOfInitialization();

    /**
     * Liefert den Start-Zeitpunkt in Milli-Sekunden.
     * <p>
     * Der Start-Zeitpunkt liegt zeitlich stets vor der
     * Initialisierung, kann aber aufgrund der Granularit&auml;t
     * der JVM auf einen Zeitpunkt zusammenfallen.
     * <p>
     * Gemessen wird die Differenz in Millisekunden zwischen dem
     * Start-Zeitpunkt und Mitternacht am 1. Januar 1970 UTC.
     *
     * @return den Start-Zeitpunkt in Milli-Sekunden.
     * @see System#currentTimeMillis()
     */
    long getTimeMillisOfStartup();

    /**
     * Der Hash-Code des DataSource-Objekt das vom internen Supplier geliefert wird.
     * <p>
     * Gibt es noch kein DataSource-Objekt das vom internen Supplier geliefert
     * wurde, dann wird null zur&uuml;ckgeliefert.
     * <p>
     * Mit diese Methode kann festgestellt werden ob sich das interne DataSource-Objekt
     * (zum Beispiel durch den Aufruf von {@link LazyDataSource#invalidate()})
     * ge&auml;ndert hat oder nicht.
     *
     * @return der Hash-Code des DataSource-Objekts das vom internen Supplier
     * geliefert wird.
     * @see System#identityHashCode(Object)
     */
    int identityHashCode();

    /**
     * Invalidiert diese DataSource.
     * <p>
     * Dies f&uuml;hrt meistens dazu das die DataSource beim
     * n&auml;chsten API-Zugriff erneut erzeugt und initialisiert
     * werden muss.
     */
    void invalidate();

    /**
     * Bei {@code true} wird die zugrundeliegende DataSource
     * erst beim ersten API-Zugriff erzeugt und
     * initialisiert.
     * <p>
     * Der Standardwert ist {@code true}.
     *
     * @return {@code true} Initialisierung bis zum ersten API-Zugriff
     * hinausgez&ouml;gert werden soll.
     */
    boolean isLazyInit();

    /**
     * Legt mit {@code true} fest das die zugrundeliegende DataSource
     * erst beim ersten API-Zugriff erzeugt und initialisiert
     * werden soll.
     * <p>
     * Der Standardwert ist {@code true}.
     *
     * @param lazyInit {@code true} wenn die Initialisierung bis zum
     *                 ersten API-Zugriff hinausgez&ouml;gert
     *                 werden soll.
     */
    void setLazyInit(boolean lazyInit);

    /**
     * Liefert {@code true} wenn die zugrundeliegende DataSource
     * bereits erzeugt und initialisiert wurde.
     *
     * @return {@code true} wenn DataSource bereits erzeugt und
     * initialisiert wurde.
     */
    boolean isPresent();
}
