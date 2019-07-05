package eu.dirk.haase.hibernate.cache.version_3_2;

import eu.dirk.haase.hibernate.jdbc.ThreadLocalResourceRegistry;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThreadLocalMemoryCache implements Cache {

    private final String regionName;
    private final ThreadLocalResourceRegistry<String, Cache> threadLocalCacheRegistry;

    public ThreadLocalMemoryCache(final String regionName) {
        this.regionName = regionName;
        this.threadLocalCacheRegistry = ThreadLocalResourceRegistry.newInstance(ThreadLocalResourceRegistry.RefType.HARD);
    }

    @Override
    public void clear() throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        cache.clear();
    }

    @Override
    public void destroy() throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        cache.destroy();
    }

    @Override
    public Object get(Object key) throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.get(key);
    }

    @Override
    public long getElementCountInMemory() {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.getElementCountInMemory();
    }

    @Override
    public long getElementCountOnDisk() {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.getElementCountOnDisk();
    }

    @Override
    public String getRegionName() {
        return regionName;
    }

    @Override
    public long getSizeInMemory() {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.getSizeInMemory();
    }

    @Override
    public int getTimeout() {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.getTimeout();
    }

    @Override
    public void lock(Object key) throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        cache.lock(key);
    }

    @Override
    public long nextTimestamp() {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.nextTimestamp();
    }

    @Override
    public void put(Object key, Object value) throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        cache.put(key, value);
    }

    @Override
    public Object read(Object key) throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.read(key);
    }

    @Override
    public void remove(Object key) throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        cache.remove(key);
    }

    @Override
    public Map toMap() {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        return cache.toMap();
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + regionName + ')';
    }

    @Override
    public void unlock(Object key) throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        cache.unlock(key);
    }

    @Override
    public void update(Object key, Object value) throws CacheException {
        final Cache cache = threadLocalCacheRegistry.computeIfAbsent(regionName, (k) -> new HashMapCache());
        cache.update(key, value);
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
