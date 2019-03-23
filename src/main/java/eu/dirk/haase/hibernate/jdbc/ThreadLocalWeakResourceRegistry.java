package eu.dirk.haase.hibernate.jdbc;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;


public final class ThreadLocalWeakResourceRegistry<K, V1> implements ThreadLocalResourceRegistry<K, V1> {

    private final RefType refType;
    private final AtomicReference<ThreadLocal<Map<K, Reference<V1>>>> threadLocalRef;

    public ThreadLocalWeakResourceRegistry() {
        this(RefType.WEAK);
    }

    public ThreadLocalWeakResourceRegistry(final RefType refType) {
        this.threadLocalRef = new AtomicReference<>(ThreadLocal.withInitial(HashMap::new));
        this.refType = refType;
    }

    @Override
    public void clearAll() {
        this.threadLocalRef.set(ThreadLocal.withInitial(HashMap::new));
    }

    @Override
    public void clearCurrent() {
        final ThreadLocal<Map<K, Reference<V1>>> threadLocal = this.threadLocalRef.get();
        threadLocal.remove();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V2 extends V1> V2 getCurrent(final K key) {
        return get(key, (k, m) -> m.get(k));
    }

    public boolean isCurrentExisting(final K key) {
        return getCurrent(key) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V2 extends V1> V2 newIfAbsent(final K key, final Function<? super K, ? extends V1> newInstance) {
        final Map<K, Reference<V1>> localMap = getLocalMap();
        final Function<? super K, ? extends Reference<V1>> newInstanceRef = (k) -> newReference(newInstance.apply(k));
        final Reference<V1> ref = localMap.computeIfAbsent(key, newInstanceRef);
        return (V2) ref.get();
    }

    public <V1> Reference<V1> newReference(final V1 value) {
        if (refType == RefType.WEAK) {
            return new WeakReference<V1>(value);
        } else {
            return new SoftReference<V1>(value);
        }
    }

    @Override
    public Runnable clearFunction(K key) {
        final Map<K, Reference<V1>> localMap = getLocalMap();
        return () -> localMap.remove(key);
    }

    @Override
    public <V2 extends V1> V2 removeCurrent(final K key) {
        return get(key, (k, m) -> m.remove(k));
    }

    @SuppressWarnings("unchecked")
    private <V2 extends V1> V2 get(final K key, final BiFunction<K, Map<K, Reference<V1>>, Reference<V1>> functionOnMap) {
        final Map<K, Reference<V1>> localMap = getLocalMap();
        final Reference<V1> ref = functionOnMap.apply(key, localMap);
        return (V2) (ref != null ? ref.get() : null);
    }

    private Map<K, Reference<V1>> getLocalMap() {
        final ThreadLocal<Map<K, Reference<V1>>> threadLocal = this.threadLocalRef.get();
        return threadLocal.get();
    }


    public enum RefType {
        WEAK, SOFT;
    }

}
