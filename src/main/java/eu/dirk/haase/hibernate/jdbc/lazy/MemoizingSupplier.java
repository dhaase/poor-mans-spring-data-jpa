package eu.dirk.haase.hibernate.jdbc.lazy;

import java.io.Serializable;
import java.util.function.Supplier;


/**
 * Ein {@link Supplier} das den R&uuml;ckgabewert aus dem Aufruf
 * {@link Supplier#get()} vom internen {@link Supplier} zwischenspeichert
 * und daher nur einmal auswertet.
 * <p>
 * Serialisiert wird in diesem {@link Supplier} stets nur der Lambda-Ausdruck
 * oder der interne {@link Supplier}. Der R&uuml;ckgabewert aus dem Aufruf
 * {@link Supplier#get()} vom internen {@link Supplier} bleibt durch die
 * Serialisierung nicht erhalten.
 * Es wird daher nach der Deserialisierung der internen {@link Supplier}
 * stets erneut ausgewertet.
 * <p>
 * Dieser {@link Supplier} ist in jedem Falle serialisierbar, auch wenn
 * das Objekt das mit {@link Supplier#get()} geliefert wird nicht
 * serialisierbar ist.
 *
 * @author Haase, Dirk (Dirk.Haase&#64;ing-diba.de)
 * @since 25.09.2018
 */
public class MemoizingSupplier<T> implements Supplier<T>, Serializable {


    private static final long serialVersionUID = 0;

    private final Supplier<T> delegate;

    /**
     * Internes Lock-Objekt muss serialisierbar sein.
     */
    private final Object lockMonitor = new LockMonitor();

    private transient volatile boolean isInitialized;

    // Die Variable 'value' muss nicht volatile sein da sie
    // mit der volatile Variable 'isInitialized' automatisch
    // auch aufgefrischt wird.
    private transient T value;


    public MemoizingSupplier(final Supplier<T> delegate) {
        this.delegate = delegate;
    }


    @Override
    public T get() {
        // Eine Variante von Double Checked Locking.
        if (!isInitialized) {
            synchronized (lockMonitor) {
                if (!isInitialized) {
                    final T t = delegate.get();
                    value = t;
                    isInitialized = true;
                    return t;
                }
            }
        }
        return value;
    }


    public boolean isPresent() {
        if (isInitialized) {
            return (value != null);
        }
        return false;
    }


    public void invalidate() {
        synchronized (lockMonitor) {
            isInitialized = false;
        }
    }


    @Override
    public String toString() {
        return "Suppliers.memoize(" + (isInitialized ? "<supplier that returned " + value + ">" : delegate) + ")";
    }


    private static class LockMonitor implements Serializable {


        private final static long serialVersionUID = 0;

    }
}



