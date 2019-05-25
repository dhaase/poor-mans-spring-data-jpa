package eu.dirk.haase.hibernate.jdbc.lazy.supplier;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Wird von spezialisierten Supplier implementiert die ein DataSource-Objekt
 * liefern.
 *
 * @param <T> der generische Typ der DataSource.
 * @see DataSource
 * @see XADataSource
 */
public interface DataSourceSupplier<T extends CommonDataSource> extends Supplier<T> {
    /**
     * Liefert die Interfaces die das Objekt implementiert das vom
     * Supplier geliefert wird.
     *
     * @return die Interfaces die das Objekt implementiert das vom
     * Supplier geliefert wird.
     */
    Set<Class<?>> getApiInterfaceSet();

    /**
     * Liefert eine Beschreibung dieses Suppliers.
     *
     * @return eine Beschreibung dieses Suppliers.
     */
    String getDescription();

    /**
     * Liefert {@code true} wenn an diesem Supplier bereits die
     * {@link #get()}-Methode aufgerufen wurde.
     *
     * @return {@code true} wenn an diesem Supplier bereits die
     * {@link #get()}-Methode aufgerufen wurde.
     */
    boolean isInitialized();

    /**
     * Setzt eine Beschreibung dieses Suppliers.
     *
     * @param description eine Beschreibung dieses Suppliers.
     */
    void setDescription(String description);

    /**
     * Liefert den Protokollschreiber f&uuml;r das DataSource-Objekt
     * (das von diesem Supplier geliefert wird) zur&uuml;ck.
     *
     * @return den Protokollschreiber f&uuml;r das DataSource-Objekt.
     */
    PrintWriter getLogWriter();

    /**
     * Legt den Protokollschreiber f&uuml;r das DataSource-Objekt
     * (das von diesem Supplier geliefert wird) auf das angegebene
     * {@link PrintWriter}-Objekt fest.
     * <p>
     * Der {@link PrintWriter} ist ein Zeichenausgabestrom, auf den
     * alle Protokollierungs- und Verfolgungsnachrichten f&uuml;r diese
     * Datenquelle gedruckt werden.
     *
     * @param logWriter der neue Protokollschreiber; Zum Deaktivieren
     *                  der Protokollierung auf {@code null} setzen
     * @throws SQLException wenn ein Datenbankzugriffsfehler auftritt.
     * @see CommonDataSource#setLogWriter(PrintWriter)
     * @see DataSource#setLogWriter(PrintWriter)
     */
    void setLogWriter(final PrintWriter logWriter);

    /**
     * Liefert die maximale Zeit in Sekunden, auf das das DataSource-Objekt
     * (das von diesem Supplier geliefert wird) wartet, w&auml;hrend sie versucht,
     * eine Verbindung zu einer Datenbank herzustellen.
     *
     * @return die maximale Zeit in Sekunden f&uuml;r den Login.
     */
    int getLoginTimeout();

    /**
     * Legt die maximale Zeit in Sekunden fest, auf das das DataSource-Objekt
     * (das von diesem Supplier geliefert wird) wartet, w&auml;hrend sie versucht,
     * eine Verbindung zu einer Datenbank herzustellen.
     * <p>
     * Ein Wert von Null gibt an, dass das Zeitlimit das Standard-Systemzeitlimit
     * ist, falls es eins gibt.
     *
     * @param loginTimeoutSeconds das Zeitlimit für die Datenquellenanmeldung.
     * @throws SQLException wenn ein Datenbankzugriffsfehler auftritt.
     * @see CommonDataSource#setLoginTimeout(int)
     * @see DataSource#setLoginTimeout(int)
     */
    void setLoginTimeout(int loginTimeoutSeconds);

    /**
     * Liefert den Zeitpunkt der Initialisierung in Milli-Sekunden.
     * <p>
     * Gemessen wird die Differenz in Millisekunden zwischen den Zeitpunkt
     * der Initialisierung und Mitternacht am 1. Januar 1970 UTC.
     *
     * @return den Zeitpunkt der Initialisierung in Milli-Sekunden.
     * @see System#currentTimeMillis()
     */
    long getTimeMillisOfInitialization();

    /**
     * Invalidiert diesen Supplier.
     */
    void invalidate();

}
