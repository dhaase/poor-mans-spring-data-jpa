package eu.dirk.haase.hibernate.jdbc;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.function.Function;


/**
 * Registry um Ressourcen f&uuml;r den jeweils aktuellen Thread zu
 * speichern und auszugeben.
 *
 * @param <K1> der generische Typ der Keys.
 * @param <V1> der generische Typ dieser Resource.
 */
public interface ThreadLocalResourceRegistry<K1, V1> {

    /**
     * Erzeugt eine neue Instanz dieser Registry.
     *
     * @param refType definiert mit welcher strenge die Ressourcen
     *                in der Registry gespeichert sollen.
     * @param <K2>    der generische Typ der Keys.
     * @param <V2>    der generische Typ dieser Resource.
     * @return eine neue Instanz dieser Registry.
     */
    static <K2, V2> ThreadLocalResourceRegistry<K2, V2> newInstance(final RefType refType) {
        switch (refType) {
            case WEAK:
            case SOFT:
                return new ThreadLocalResourceWeakRegistry<>(refType);
            case HARD:
                return new ThreadLocalResourceHardRegistry<>();
            default:
                throw new IllegalArgumentException("Unknown Referenz-Type: " + refType);
        }
    }

    /**
     * Erzeugt eine neue Instanz dieser Registry f&uuml;r Ressourcen
     * die als {@link WeakReference} gespeichert werden sollen.
     *
     * @param <K2> der generische Typ der Keys.
     * @param <V2> der generische Typ dieser Resource.
     * @return eine Instanz das dieses Interface implementiert.
     */
    static <K2, V2> ThreadLocalResourceRegistry<K2, V2> newInstance() {
        return newInstance(RefType.WEAK);
    }

    /**
     * Liefert die gespeicherte Ressource des aktuellen Threads.
     *
     * @param key  der Key unter der die Ressource gespeichert ist.
     * @param <V2> der generische Typ dieser Resource.
     * @return die gespeicherte Ressource des aktuellen Threads.
     */
    <V2> V2 getCurrent(K1 key);

    /**
     * Liefert {@code true} wenn die Ressource f&uuml;r den aktuellen
     * Thread gespeichert ist.
     *
     * @param key der Key unter der die Ressource gespeichert ist.
     * @return die gespeicherte Ressource des aktuellen Threads.
     */
    boolean isCurrentExisting(K1 key);

    /**
     * Liefert die gespeicherte Ressource des aktuellen Threads.
     *
     * @param key         der Key unter der die Ressource gespeichert ist.
     * @param newInstance Funktion mit der eine Ressource erzeugt
     *                    werden kann.
     * @param <V2>        der generische Typ dieser Resource.
     * @return die gespeicherte Ressource des aktuellen Threads.
     */
    <V2> V2 computeIfAbsent(K1 key, Function<? super K1, ? extends V1> newInstance);

    /**
     * Gibt alle Resourcen, unabh&auml;ngig von Threads,
     * aus dieser Registry frei.
     */
    void releaseAll();

    /**
     * Gibt alle Ressourcen des aktuellen Threads
     * aus dieser Registry frei.
     */
    void releaseCurrent();

    /**
     * Liefert Funtkion um einen Eintrag aus dieser Registry
     * freizugeben.
     *
     * @param key der Key unter der die Ressource gespeichert ist.
     * @return Funtkion um einen Eintrag aus dieser Registry
     * freizugeben.
     */
    Runnable releaseFunction(K1 key);

    /**
     * Gibt eine Ressource aus dieser Registry frei.
     *
     * @param key  der Key unter der die Ressource gespeichert ist.
     * @param <V2> der generische Typ dieser Resource.
     * @return die alte Ressource die gespeichert war.
     */
    <V2> V2 releaseCurrent(K1 key);

    /**
     * Wird implementiert von Ressourcen beim Schlie&szlig;en auch
     * ihren Eintrag aus der {@link ThreadLocalResourceRegistry}
     * entfernen sollen.
     */
    interface ReleaseFunctionAware {
        void setReleaseFunction(final Runnable releaseFunction);

        static void release(final Runnable releaseFunction) {
            if (releaseFunction != null) {
                releaseFunction.run();
            }
        }
    }

    /**
     * Marker-Interface f&uuml;r eine Ressource die an einem
     * Thread gebunden ist.
     */
    interface ThreadLocalRessource {

    }


    /**
     * Definiert mit welcher strenge die Ressourcen in der Registry gespeichert
     * sollen.
     */
    enum RefType {
        /**
         * Ressourcen werden als {@link WeakReference} gespeichert und
         * daher von der Garbage Collection verschont nur solange verschont
         * wie diese Ressource auch an anderer Stelle als st&auml;rkere Referenz
         * gespeichert ist.
         * <p>
         * Diese Ressourcen k&ouml;nnen auch aus der Registry entfernt werden,
         * ohne das diese explizit freigegeben werden m&uuml;ssen.
         */
        WEAK,
        /**
         * Ressourcen werden als {@link SoftReference} gespeichert und
         * bleiben so lange von der Garbage Collection verschont, wie
         * der Hauptspeicher ausreicht.
         * <p>
         * Wenn der Speicher knapp wird, werden die referenzierten Ressourcen
         * entfernt und die Soft-Referenzen automatisch gel&ouml;scht.
         * <p>
         * Diese Ressourcen k&ouml;nnen auch aus der Registry entfernt werden,
         * ohne das diese explizit freigegeben werden m&uuml;ssen.
         */
        SOFT,
        /**
         * Ressourcen werden als Hard- als mit der st&auml;rksten Referenz
         * gespeichert.
         * <p>
         * Diese Ressourcen solange in der Registry gespeichert bis diese
         * freigegeben werden.
         */
        HARD;
    }
}


