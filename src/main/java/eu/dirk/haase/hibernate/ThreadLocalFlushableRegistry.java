package eu.dirk.haase.hibernate;

import eu.dirk.haase.hibernate.jdbc.ThreadLocalResourceRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ThreadLocalFlushableRegistry implements FlushableRegistry {

    private final ThreadLocalResourceRegistry<ResourceType, FlushableList> registry;

    public ThreadLocalFlushableRegistry() {
        this.registry = ThreadLocalResourceRegistry.newInstance();
    }

    @Override
    public int flushAllCurrent() {
        FlushableList flushableList = registry.getCurrent(ResourceType.FlushableList);
        return (flushableList != null ? flushableList.flushAll() : 0);
    }

    @Override
    public int register(Flushable flushable) {
        FlushableList flushableList = registry.computeIfAbsent(ResourceType.FlushableList, (k) -> new FlushableList());
        return flushableList.register(flushable);
    }

    /**
     * Gibt alle Resourcen, unabh&auml;ngig von Threads,
     * aus dieser Registry frei.
     */
    @Override
    public void releaseAll() {
        registry.releaseAll();
    }

    /**
     * Gibt alle Ressourcen des aktuellen Threads
     * aus dieser Registry frei.
     */
    @Override
    public void releaseCurrent() {
        registry.releaseCurrent();
    }

    @Override
    public int sizeCurrent() {
        FlushableList flushableList = registry.getCurrent(ResourceType.FlushableList);
        return (flushableList != null ? flushableList.index2FlushableMap.size() : 0);
    }

    enum ResourceType {
        FlushableList
    }

    static class FlushableList implements ThreadLocalResourceRegistry.ReleaseFunctionAware {

        final Map<Flushable, Integer> flushable2IndexMap;
        final SortedMap<Integer, Flushable> index2FlushableMap;
        int index;
        Runnable releaseFunction;

        FlushableList() {
            this.index2FlushableMap = new TreeMap<>();
            this.flushable2IndexMap = new HashMap<>();
        }

        void flush(final Integer key, final Flushable flushable) {
            flushable.flush();
        }

        int flushAll() {
            final int countFlushed = flushAllInternal();
            releaseFunction.run();
            return countFlushed;
        }

        private int flushAllInternal() {
            final int countFlushed = this.index2FlushableMap.size();
            this.index2FlushableMap.forEach((k, f) -> flush(k, f));
            this.index2FlushableMap.clear();
            this.flushable2IndexMap.clear();
            return countFlushed;
        }

        private int flushUntilIndexIfDuplicate(final Flushable flushable) {
            final Integer untilIndex = this.flushable2IndexMap.get(flushable);
            final Integer lastIndex = lastIndex();
            if (isDuplicateFlushable(untilIndex, lastIndex)) {
                final Map<Integer, Flushable> headMap = new HashMap<>(this.index2FlushableMap.headMap(untilIndex));
                headMap.put(untilIndex, this.index2FlushableMap.get(untilIndex));
                headMap.forEach((k, f) -> flush(k, f));
                for (final Integer prevIndex : headMap.keySet()) {
                    removeInternal(prevIndex);
                }
                return headMap.size();
            }
            return 0;
        }

        private boolean isDuplicateFlushable(Integer untilIndex, Integer lastIndex) {
            return (untilIndex != null) && !untilIndex.equals(lastIndex);
        }

        private boolean isLastAndNewFlushable(Flushable flushable, Integer untilIndex, Integer lastIndex) {
            return (this.index2FlushableMap.get(untilIndex) != flushable) && untilIndex.equals(lastIndex);
        }

        private boolean isNewFlushable(Integer untilIndex, Integer lastIndex) {
            return (untilIndex == null) || !untilIndex.equals(lastIndex);
        }

        private Integer lastIndex() {
            return (this.index2FlushableMap.isEmpty() ? null : this.index2FlushableMap.lastKey());
        }

        int register(final Flushable flushable) {
            int countFlushed = flushUntilIndexIfDuplicate(flushable);
            final Integer untilIndex = this.flushable2IndexMap.get(flushable);
            final Integer lastIndex = lastIndex();
            if (isNewFlushable(untilIndex, lastIndex)) {
                registerInternal(flushable);
            } else if (isLastAndNewFlushable(flushable, untilIndex, lastIndex)) {
                countFlushed = flushAllInternal();
                registerInternal(flushable);
            }
            return countFlushed;
        }

        private void registerInternal(final Flushable flushable) {
            final int currIndex = ++index;
            this.index2FlushableMap.put(currIndex, flushable);
            this.flushable2IndexMap.put(flushable, currIndex);
        }

        private void removeInternal(final Integer index) {
            final Flushable flushable = this.index2FlushableMap.get(index);
            this.index2FlushableMap.remove(index);
            this.flushable2IndexMap.remove(flushable);
        }

        @Override
        public void setReleaseFunction(final Runnable releaseFunction) {
            this.releaseFunction = releaseFunction;
        }
    }

}
