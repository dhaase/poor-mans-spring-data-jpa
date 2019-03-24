package eu.dirk.haase.hibernate.jdbc;

import java.util.function.Function;

public interface ThreadLocalResourceRegistry<K, V1> {

    <V2> V2 getCurrent(K key);

    boolean isCurrentExisting(K key);

    <V2> V2 computeIfAbsent(K key, Function<? super K, ? extends V1> newInstance);

    void releaseAll();

    void releaseCurrent();

    Runnable releaseFunction(K key);

    <V2> V2 releaseCurrent(K key);

    interface ReleaseFunctionAware {
        void setReleaseFunction(final Runnable releaseFunction);
    }

    interface ThreadLocalResource {

    }
}


