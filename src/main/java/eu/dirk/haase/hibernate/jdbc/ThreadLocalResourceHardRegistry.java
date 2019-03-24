package eu.dirk.haase.hibernate.jdbc;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;


public final class ThreadLocalResourceHardRegistry<K, V1> implements ThreadLocalResourceRegistry<K, V1> {

    private final AtomicReference<ThreadLocal<Map<K, V1>>> threadLocalRef;

    public ThreadLocalResourceHardRegistry() {
        this.threadLocalRef = new AtomicReference<>(ThreadLocal.withInitial(HashMap::new));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V2> V2 computeIfAbsent(final K key, final Function<? super K, ? extends V1> newInstance1) {
        final Map<K, V1> localMap = getLocalMap();
        final Function<? super K, ? extends V1> newInstance2 = (k) -> initReference(k, newInstance1.apply(k));
        return (V2) localMap.computeIfAbsent(key, newInstance2);
    }

    @SuppressWarnings("unchecked")
    private <V2> V2 get(final K key, final BiFunction<K, Map<K, V1>, V1> functionOnMap) {
        final Map<K, V1> localMap = getLocalMap();
        final V1 value = functionOnMap.apply(key, localMap);
        return (V2) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V2> V2 getCurrent(final K key) {
        return get(key, (k, m) -> m.get(k));
    }

    private Map<K, V1> getLocalMap() {
        final ThreadLocal<Map<K, V1>> threadLocal = this.threadLocalRef.get();
        return threadLocal.get();
    }

    private V1 initReference(K key, V1 value) {
        if (value instanceof ThreadLocalResourceRegistry.ReleaseFunctionAware) {
            final Runnable releaseFunction = releaseFunction(key);
            ((ReleaseFunctionAware) value).setReleaseFunction(releaseFunction);
        }
        return value;
    }

    public boolean isCurrentExisting(final K key) {
        return getCurrent(key) != null;
    }

    @Override
    public void releaseAll() {
        this.threadLocalRef.set(ThreadLocal.withInitial(HashMap::new));
    }

    @Override
    public void releaseCurrent() {
        final ThreadLocal<Map<K, V1>> threadLocal = this.threadLocalRef.get();
        threadLocal.remove();
    }

    @Override
    public <V2> V2 releaseCurrent(final K key) {
        return get(key, (k, m) -> m.remove(k));
    }

    @Override
    public Runnable releaseFunction(K key) {
        final Map<K, V1> localMap = getLocalMap();
        return () -> localMap.remove(key);
    }


    public enum RefType {
        WEAK, SOFT;
    }

}
