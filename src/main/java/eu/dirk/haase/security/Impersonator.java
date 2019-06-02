package eu.dirk.haase.security;

import java.util.function.Supplier;

/**
 * Mit einem {@link Impersonator} ist es m&ouml;glich den aktuellen
 * User tempor&auml;r zu einem anderen User zu &auml;ndern.
 * <p>
 * Die User-&Auml;nderung wird in die aktuelle Datenbank-Session (GLOBALS)
 * &uuml;bertragen, so dass Datenbank-&Auml;nderungen mit dem neuen User
 * durchgef&uuml;hrt werden.
 * <p>
 * Es k&ouml;nnen zwei M&ouml;glichkeiten empfohlen werden k&ouml;nnen:
 * <p>
 * 1. User-&Auml;nderung mittels try-with-resources Statement:
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
 * 2. User-&Auml;nderung mittels Lambda-Ausdruck:
 * <pre><code>
 * // Bis zu dieser Stelle werden die folgenden Datenbank-Statenments
 * // noch mit dem User alten User ausgefuehrt.
 * impersonator.impersonate("neuerUser", () -&gt; {
 *    // Die folgenden Datenbank-Statenments werden unter dem User
 *    // "neuerUser" ausgefuehrt.
 *    ...
 * });
 * // Ab hier werden die folgenden Datenbank-Statenments wieder
 * // unter dem User alten User ausgefuehrt.
 * </code></pre>
 * <p>
 * Es k&ouml;nnen auch die User-&Auml;nderungen geschachtelt werden:
 * <pre><code>
 * // Bis zu dieser Stelle werden die folgenden Datenbank-Statenments
 * // noch mit dem User alten User ausgefuehrt.
 * impersonator.impersonate("user-1", () -&gt; {
 *    // Die folgenden Datenbank-Statenments werden unter dem User
 *    // "user-1" ausgefuehrt.
 *    impersonator.impersonate("user-2", () -&gt; {
 *       // Die folgenden Datenbank-Statenments werden unter dem User
 *       // "user-2" ausgefuehrt.
 *       impersonator.impersonate("user-3", () -&gt; {
 *          // Die folgenden Datenbank-Statenments werden unter dem User
 *          // "user-3" ausgefuehrt.
 *       });
 *       // Hier ist wieder User "user-2" aktiv.
 *    });
 *   // Hier ist wieder User "user-1" aktiv.
 * });
 * // Ab hier werden die folgenden Datenbank-Statenments wieder
 * // unter dem User alten User ausgefuehrt.
 * </code></pre>
 */
public interface Impersonator {

    /**
     * Erzeugt einen neuen {@code Impersonator} aus einer Standard-Implementation.
     *
     * @param flushAllRunnable die Flush-Funktion um vorgelagerte potentielle Datenbank-
     *                         &Auml;nderungen, die in der Vergangenheit aufgelaufen sind,
     *                         anzuwenden bevor zu einem neuen User gewechselt werden soll
     *                         (flushed).
     * @return der neue {@code Impersonator}.
     */
    static Impersonator newImpersonator(final Runnable flushAllRunnable) {
        return new ThreadLocalImpersonator(flushAllRunnable);
    }

    /**
     * L&ouml;scht den aktuellen und tempor&auml;ren User des
     * ausf&uuml;hrenden Threads.
     */
    void clear();

    /**
     * Liefert eine {@link Supplier} der den jeweils g&uuml;ltigen
     * User liefert.
     * <p>
     * Wird {@code null} geliefert dann wurde f&uuml;r den
     * ausf&uuml;hrenden Thread kein (neuer) User gesetzt.
     * Es ist dann kein Identit&auml;tswechsel (=Impersonation)
     * aktiv.
     *
     * @return ein {@link Supplier} der den jeweils g&uuml;ltigen
     * User liefert.
     */
    Supplier<String> currentUserSupplier();

    /**
     * &Auml;ndert den User unter dem das angegebene Kommando
     * ausgef&uuml;hrt werden soll.
     * <p>
     * Bevor zu einem neuen User gewechselt wird, werden automatisch
     * eventuelle vorgelagerte potentielle Datenbank-&Auml;nderungen,
     * die in der Vergangenheit aufgelaufen sind, abgesetzt (flushed).
     * <p>
     * Beispiel:
     * <pre><code>
     * // Bis zu dieser Stelle werden die folgenden Datenbank-Statenments
     * // noch mit dem User alten User ausgefuehrt.
     * impersonator.impersonate("neuerUser", () -&gt; {
     *    // Die folgenden Datenbank-Statenments werden unter dem User
     *    // "neuerUser" ausgefuehrt.
     *    ...
     * });
     * // Ab hier werden die folgenden Datenbank-Statenments wieder
     * // unter dem User alten User ausgefuehrt.
     * </code></pre>
     *
     * @param runAsUser der User der w&auml;hrend des Kommandos aktiv
     *                  sein soll.
     * @param command   das Kommando das unter dem neuen User ausgef&uuml;hrt
     *                  werden soll.
     */
    void impersonate(final String runAsUser, final Runnable command);


    /**
     * &Auml;ndert den User f&uuml;r den ausf&uuml;hrenden Thread um
     * nachfolgende Datenbank-Statenments unter dem ge&auml;nderten
     * User auszuf&uuml;hren.
     * <p>
     * Beendet wird der tempor&auml;re Userwechsel mit der Methode
     * {@link ImpersonationContext#close()}.
     * <p>
     * Bevor zu einem neuen User gewechselt wird, werden automatisch
     * eventuelle vorgelagerte potentielle Datenbank-&Auml;nderungen,
     * die in der Vergangenheit aufgelaufen sind, abgesetzt (flushed).
     * <p>
     * Wird bei geschachtelten Kontext-Objekten nur das &auml;ssere
     * Kontext-Objekt geschlossen, dann werden automatisch alle inneren
     * Kontext-Objekte geschlossen.
     * <p>
     * Es wird empfohlen diese Methode nur Mittels des try-with-resources
     * Idioms einzusetzen:
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
     * Achtung: Wird der tempor&auml;ren Userwechsel nicht mittels
     * {@code ImpersonationContext.close()} beendet dann bleibt
     * er solange bestehen bis sich der Service-Call von selbst
     * beendet hat.
     *
     * @param runAsUser der User zum der ausf&uuml;hrenden Thread ge&auml;ndert
     *                  werden soll.
     * @return Kontext-Objekt um den tempor&auml;ren Userwechsel mittels
     * {@code ImpersonationContext.close()} zu beenden.
     */
    ImpersonationContext impersonate(final String runAsUser);

}
