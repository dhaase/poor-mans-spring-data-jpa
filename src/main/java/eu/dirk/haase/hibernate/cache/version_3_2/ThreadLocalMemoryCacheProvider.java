package eu.dirk.haase.hibernate.cache.version_3_2;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadLocalMemoryCacheProvider implements CacheProvider {

    private final static Map<String, ThreadLocalMemoryCache> regionCacheMap = new ConcurrentHashMap<>();

    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        return regionCacheMap.computeIfAbsent(regionName, (k)->new ThreadLocalMemoryCache(regionName));
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }

    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param properties current configuration settings.
     */
    public void start(Properties properties) throws CacheException {
    }

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop() {
    }

}
