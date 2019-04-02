package eu.dirk.haase.hibernate;

import eu.dirk.haase.hibernate.jdbc.ThreadLocalResourceRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ThreadLocalFlushableRegistry implements FlushableRegistry {

    private final ThreadLocalResourceRegistry<ResourceType, FlushableList> registry;

    public ThreadLocalFlushableRegistry() {
        this.registry = ThreadLocalResourceRegistry.newInstance(ThreadLocalResourceRegistry.RefType.HARD);
    }

    @Override
    public int flushAllCurrent() {
        FlushableList flushableList = registry.getCurrent(ResourceType.FlushableList);
        return (flushableList != null ? flushableList.flushAllAndRelease() : 0);
    }

    @Override
    public boolean isCurrentlyInFlushSequence() {
        FlushableList flushableList = registry.getCurrent(ResourceType.FlushableList);
        return (flushableList != null ? flushableList.isInFlushSequence : false);
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

        final static boolean IS_PERSISTENCE_CONTEXT_EM_COUPLED = false;
        final Map<Flushable, Integer> flushable2IndexMap;
        final SortedMap<Integer, Flushable> index2FlushableMap;
        int index;
        boolean isInFlushSequence;
        Runnable releaseFunction;

        FlushableList() {
            this.isInFlushSequence = false;
            this.index2FlushableMap = new TreeMap<>();
            this.flushable2IndexMap = new HashMap<>();
        }

        private int flushAll() {
            if (!isInFlushSequence) {
                try {
                    isInFlushSequence = true;
                    final int countFlushed = this.index2FlushableMap.size();
                    final Map<Integer, Flushable> flushableMap = new TreeMap<>(this.index2FlushableMap);
                    flushableMap.forEach((k, f) -> flushAndRemove(k, f));
                    return countFlushed;
                } finally {
                    isInFlushSequence = false;
                }
            }
            return 0;
        }

        int flushAllAndRelease() {
            final int countFlushed = flushAll();
            ThreadLocalResourceRegistry.ReleaseFunctionAware.release(releaseFunction);
            return countFlushed;
        }

        void flushAndRemove(final Integer index, final Flushable flushable) {
            if (flushable instanceof SequenceFlushable) {
                final SequenceFlushable sequenceFlushable = (SequenceFlushable) flushable;
                sequenceFlushable.setSequenceFlushableFunction(() -> sequenceFlushable.realFlush());
                sequenceFlushable.realFlush();
            } else {
                flushable.flush();
            }
            removeInternal(index);
        }

        private int sequenceFlush(Integer untilIndex) {
            if (!isInFlushSequence && this.index2FlushableMap.containsKey(untilIndex)) {
                try {
                    isInFlushSequence = true;
                    final Map<Integer, Flushable> headMap = new TreeMap<>(this.index2FlushableMap.headMap(untilIndex));
                    headMap.put(untilIndex, this.index2FlushableMap.get(untilIndex));
                    headMap.forEach((k, f) -> flushAndRemove(k, f));
                    return headMap.size();
                } finally {
                    isInFlushSequence = false;
                }
            } else {
                return 0;
            }
        }

        private int sequenceFlushIfKnown(final Flushable flushable) {
            final Integer untilIndex = this.flushable2IndexMap.get(flushable);
            final Integer lastIndex = lastIndexOverall();
            if (isKnownButNotLastFlushable(untilIndex, lastIndex)) {
                return sequenceFlush(untilIndex);
            }
            return 0;
        }

        private boolean isKnownButNotLastFlushable(Integer untilIndex, Integer lastIndex) {
            return (untilIndex != null) && !untilIndex.equals(lastIndex);
        }

        private boolean isLastAndKnownButNotIdentical(Flushable flushable, Integer flushableIndex, Integer
                lastIndex) {
            return (this.index2FlushableMap.get(flushableIndex) != flushable)
                    && this.index2FlushableMap.get(flushableIndex).equals(flushable)
                    && flushableIndex.equals(lastIndex);
        }

        private boolean isUnknownFlushable(Integer flushableIndex) {
            return (flushableIndex == null);
        }

        private Integer lastIndexOverall() {
            return (this.index2FlushableMap.isEmpty() ? null : this.index2FlushableMap.lastKey());
        }

        int register(final Flushable flushable) {
            int countFlushed = sequenceFlushIfKnown(flushable);
            final Integer flushableIndex = this.flushable2IndexMap.get(flushable);
            if (isUnknownFlushable(flushableIndex)) {
                registerInternal(flushable);
            } else if (IS_PERSISTENCE_CONTEXT_EM_COUPLED
                    && isLastAndKnownButNotIdentical(flushable, flushableIndex, lastIndexOverall())) {
                countFlushed = flushAll();
                registerInternal(flushable);
            }
            return countFlushed;
        }

        private void registerInternal(final Flushable flushable) {
            final int currIndex = ++index;
            this.index2FlushableMap.put(currIndex, flushable);
            this.flushable2IndexMap.put(flushable, currIndex);
            if (flushable instanceof SequenceFlushable) {
                ((SequenceFlushable) flushable).setSequenceFlushableFunction(() -> sequenceFlush(currIndex));
            }
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
