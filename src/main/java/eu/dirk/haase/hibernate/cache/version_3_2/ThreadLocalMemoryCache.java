package eu.dirk.haase.hibernate.cache.version_3_2;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThreadLocalMemoryCache implements Cache {

    private final static ThreadLocal<Cache> threadLocalCache = ThreadLocal.withInitial(() -> new HashMapCache());

    private final String regionName;

    public ThreadLocalMemoryCache(final String regionName) {
        this.regionName = regionName;
    }

    @Override
    public void clear() throws CacheException {
        threadLocalCache.get().clear();
    }

    @Override
    public void destroy() throws CacheException {
        threadLocalCache.get().destroy();
    }

    @Override
    public Object get(Object key) throws CacheException {
        return threadLocalCache.get().get(key);
    }

    @Override
    public long getElementCountInMemory() {
        return threadLocalCache.get().getElementCountInMemory();
    }

    @Override
    public long getElementCountOnDisk() {
        return threadLocalCache.get().getElementCountOnDisk();
    }

    @Override
    public String getRegionName() {
        return regionName;
    }

    @Override
    public long getSizeInMemory() {
        return threadLocalCache.get().getSizeInMemory();
    }

    @Override
    public int getTimeout() {
        return threadLocalCache.get().getTimeout();
    }

    @Override
    public void lock(Object key) throws CacheException {
        threadLocalCache.get().lock(key);
    }

    @Override
    public long nextTimestamp() {
        return threadLocalCache.get().nextTimestamp();
    }

    @Override
    public void put(Object key, Object value) throws CacheException {
        threadLocalCache.get().put(key, value);
    }

    @Override
    public Object read(Object key) throws CacheException {
        return threadLocalCache.get().read(key);
    }

    @Override
    public void remove(Object key) throws CacheException {
        threadLocalCache.get().remove(key);
    }

    @Override
    public Map toMap() {
        return threadLocalCache.get().toMap();
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + regionName + ')';
    }

    @Override
    public void unlock(Object key) throws CacheException {
        threadLocalCache.get().unlock(key);
    }

    @Override
    public void update(Object key, Object value) throws CacheException {
        threadLocalCache.get().update(key, value);
    }

    /**
     * A lightweight implementation of the <tt>Cache</tt> interface
     *
     * @author Gavin King
     */
    static class HashMapCache implements Cache {

        final Map<Object, Object> backstoreMap;
        final Map<Object, Object> unmodifiableBackstoreMap;

        HashMapCache() {
            this.backstoreMap = new HashMap<>();
            this.unmodifiableBackstoreMap = Collections.unmodifiableMap(backstoreMap);
        }

        public void clear() throws CacheException {
            backstoreMap.clear();
        }

        public void destroy() throws CacheException {

        }

        public Object get(Object key) throws CacheException {
            return backstoreMap.get(key);
        }

        public long getElementCountInMemory() {
            return backstoreMap.size();
        }

        public long getElementCountOnDisk() {
            return 0;
        }

        @Override
        public String getRegionName() {
            throw new UnsupportedOperationException();
        }

        public long getSizeInMemory() {
            return -1;
        }

        public int getTimeout() {
            return Timestamper.ONE_MS * 60000; //ie. 60 seconds
        }

        public void lock(Object key) throws CacheException {
            // local cache, so we use synchronization
        }

        public long nextTimestamp() {
            return Timestamper.next();
        }

        public void put(Object key, Object value) throws CacheException {
            backstoreMap.put(key, value);
        }

        public Object read(Object key) throws CacheException {
            return backstoreMap.get(key);
        }

        public void remove(Object key) throws CacheException {
            backstoreMap.remove(key);
        }

        public Map<Object, Object> toMap() {
            return this.unmodifiableBackstoreMap;
        }

        public void unlock(Object key) throws CacheException {
            // local cache, so we use synchronization
        }

        public void update(Object key, Object value) throws CacheException {
            put(key, value);
        }


    }
}
