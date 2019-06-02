package eu.dirk.haase.security;

/**
 * Dieses Kontext-Objekt wird durch {@link Impersonator#impersonate(java.lang.String)}
 * erzeugt.
 * <p>
 * Dieses Kontext-Objekt sollte nur mittels des try-with-resources Idioms
 * gesetzt werden:
 * <pre><code>
 * // Bis zu dieser Stelle werden die folgenden Datenbank-Statenments
 * // noch mit dem User alten User ausgefuehrt.
 * try (ImpersonationContext ctx1 = impersonator.impersonate("neuerUser")) {
 *    // Die folgenden Datenbank-Statenments werden unter dem User
 *    // "neuerUser" ausgefuehrt.
 *    ...
 * }
 * // Ab hier werden die folgenden Datenbank-Statenments wieder
 * // unter dem User alten User ausgefuehrt.
 * </code></pre>
 * <p>
 * Beendet wird der tempor&auml;re Userwechsel mit der Methode
 * {@link ImpersonationContext#close()}.
 */
public interface ImpersonationContext extends AutoCloseable {
    /**
     * Beendet den tempor&auml;ren Userwechsel.
     * <p>
     * Achtung: Wird der tempor&auml;ren Userwechsel nicht mittels
     * {@link ImpersonationContext#close()} beendet dann bleibt
     * er solange bestehen bis sich der Service-Call von selbst
     * beendet hat.
     */
    void close();

    /**
     * Liefert den vorhergehenden User.
     *
     * @return der vorhergehende User.
     */
    String lastUser();

    /**
     * Der aktuelle User zu dem dieser Kontext gewechselt
     * ist.
     *
     * @return aktuelle User zu dem dieser Kontext
     * gewechselt ist.
     */
    String currentUser();

    /**
     * Liefert {@code true} wenn dieser Kontext bereits
     * geschlossen ist.
     *
     * @return {@code true} wenn dieser Kontext bereits
     * geschlossen ist.
     */
    boolean isClosed();
}
