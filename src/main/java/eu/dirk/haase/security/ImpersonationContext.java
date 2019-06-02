package eu.dirk.haase.security;

/**
 * &Auml;ndert den User f&uuml;r den ausf&uuml;hrenden Thread um
 * nachfolgende Datenbank-Statenments unter dem ge&auml;nderten
 * User auszuf&uuml;hren.
 * <p>
 * Bevor zu einem neuen User gewechselt wird, werden automatisch
 * eventuell vorgelagerte potentielle Datenbank-&Auml;nderungen,
 * die in der Vergangenheit aufgelaufen sind, abgesetzt (flushed).
 * <p>
 * Dieses Kontext-Objekt sollte nur mittels des try-with-resources
 * Idioms gesetzt werden:
 * <pre><code>
 * // Bis zu dieser Stelle werden die folgenden Datenbank-Statenments
 * // noch mit dem User alten User ausgefuehrt.
 * try (final ImpersonationContext ctx1 = impersonator.impersonate("neuerUser")) {
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
     * Beendet den aktuellen tempor&auml;ren Userwechsel.
     * <p>
     * Der n&auml;chst &uuml;bergeordnete User wird automatisch
     * aktiviert.
     * <p>
     * Achtung: Wird der tempor&auml;ren Userwechsel nicht mittels
     * {@link #close()} beendet dann bleibt er solange bestehen bis
     * sich der Service-Call von selbst beendet hat.
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
