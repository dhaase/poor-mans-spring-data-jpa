package eu.dirk.haase.hibernate.jdbc;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Registry um Ressourcen f&uuml;r den jeweils aktuellen Thread zu speichern
 * und auszugeben.
 * <p>
 * Die Ressourcen werden, im Gegensatz zu {@link ThreadLocalResourceHardRegistry}
 * entweder als Weak- oder als Softreferenz gespeichert (siehe {@link WeakReference}
 * und {@link SoftReference}).
 *
 * @param <K>  der generische Typ der Keys.
 * @param <V1> der generische Typ dieser Ressource.
 */
public final class ThreadLocalResourceWeakRegistry<K, V1> implements ThreadLocalResourceRegistry<K, V1> {

    private final RefType refType;
    private final AtomicReference<ThreadLocal<Map<K, Reference<V1>>>> threadLocalRef;

    public ThreadLocalResourceWeakRegistry() {
        this(RefType.WEAK);
    }

    public ThreadLocalResourceWeakRegistry(final RefType refType) {
        this.threadLocalRef = new AtomicReference<>(ThreadLocal.withInitial(HashMap::new));
        this.refType = refType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V2> V2 computeIfAbsent(final K key, final Function<? super K, ? extends V1> newInstance) {
        final Map<K, Reference<V1>> localMap = getLocalMap();
        final Function<? super K, ? extends Reference<V1>> newInstanceRef = (k) -> newReference(k, newInstance.apply(k));
        final Reference<V1> ref1 = localMap.computeIfAbsent(key, newInstanceRef);
        final V2 value = (V2) ref1.get();
        if (value == null) {
            // Der Wert kann 'null' sein wenn der Garbage-Collector
            // den Wert in der (Weak-/Soft-) Reference geloescht hat.
            // Der Eintrag wird aus der Map geloescht:
            localMap.remove(key);
            // Jetzt wird ein neuer Wert erzeugt:
            final Reference<V1> ref2 = localMap.computeIfAbsent(key, newInstanceRef);
            return (V2) ref2.get();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private <V2> V2 get(final K key, final BiFunction<K, Map<K, Reference<V1>>, Reference<V1>> functionOnMap) {
        final Map<K, Reference<V1>> localMap = getLocalMap();
        final Reference<V1> ref = functionOnMap.apply(key, localMap);
        return (V2) (ref != null ? ref.get() : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V2> V2 getCurrent(final K key) {
        return get(key, (k, m) -> m.get(k));
    }

    private Map<K, Reference<V1>> getLocalMap() {
        final ThreadLocal<Map<K, Reference<V1>>> threadLocal = this.threadLocalRef.get();
        return threadLocal.get();
    }

    private <V2> void initReference(K key, V2 value) {
        if (value instanceof ThreadLocalResourceRegistry.ReleaseFunctionAware) {
            final Runnable releaseFunction = releaseFunction(key);
            ((ReleaseFunctionAware) value).setReleaseFunction(releaseFunction);
        }
    }

    public boolean isCurrentExisting(final K key) {
        return getCurrent(key) != null;
    }

    public <V2> Reference<V2> newReference(final K key, final V2 value) {
        initReference(key, value);
        if (refType == RefType.WEAK) {
            return new WeakReference<V2>(value);
        } else {
            return new SoftReference<V2>(value);
        }
    }

    @Override
    public void releaseAll() {
        this.threadLocalRef.set(ThreadLocal.withInitial(HashMap::new));
    }

    @Override
    public void releaseCurrent() {
        final ThreadLocal<Map<K, Reference<V1>>> threadLocal = this.threadLocalRef.get();
        threadLocal.remove();
    }

    @Override
    public <V2> V2 releaseCurrent(final K key) {
        return get(key, (k, m) -> m.remove(k));
    }

    @Override
    public Runnable releaseFunction(K key) {
        final Map<K, Reference<V1>> localMap = getLocalMap();
        return () -> localMap.remove(key);
    }


}
