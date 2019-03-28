package eu.dirk.haase.hibernate.jdbc;

import java.util.function.Supplier;

public class Cache {

    private final CompountKey[] store;

    Cache() {
        this.store = new CompountKey[3];
    }

    public Object get(final Object key, final Object category, Supplier<CompountKey> newInstance) {
        for (int i = 0; store.length > i; ++i) {
            if ((store[i] != null) &&
                    store[i].key().equals(key) &&
                    store[i].category().equals(category)) {
                return store[i];
            }
        }
        final CompountKey compountKey = newInstance.get();
        for (int i = 0; store.length > i; ++i) {
            if (store[i] == null) {
                store[i] = compountKey;
                return compountKey;
            }
        }
        store[0] = compountKey;
        return compountKey;
    }

    static interface CompountKey {
        Object category();

        Object key();
    }
}
