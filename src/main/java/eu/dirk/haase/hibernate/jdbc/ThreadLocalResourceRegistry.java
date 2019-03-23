package eu.dirk.haase.hibernate.jdbc;

import java.util.function.Function;

public interface ThreadLocalResourceRegistry<K, V1> {

    Runnable clearFunction(K key);

    <V2 extends V1> V2 removeCurrent(K key);

    void clearAll();

    void clearCurrent();

    <V2 extends V1> V2 newIfAbsent(K key, Function<? super K, ? extends V1> newInstance);

    <V2 extends V1> V2 getCurrent(K key);

    boolean isCurrentExisting(K key);


}


