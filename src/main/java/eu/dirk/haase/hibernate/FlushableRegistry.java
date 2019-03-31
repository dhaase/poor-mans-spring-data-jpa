package eu.dirk.haase.hibernate;

public interface FlushableRegistry {

    static FlushableRegistry newInstance() {
        return new ThreadLocalFlushableRegistry();
    }

    int flushAllCurrent();

    int register(Flushable flushable);

    void releaseAll();

    void releaseCurrent();

    int sizeCurrent();

    boolean isCurrentlyInFlushSequence();

    interface SequenceFlushable {

        void realFlush();

        void setSequenceFlushableFunction(final Runnable sequenceFlushableFunction);

        static boolean flush(final Runnable sequenceFlushableFunction) {
            if (sequenceFlushableFunction != null) {
                sequenceFlushableFunction.run();
                return true;
            }
            return false;
        }
    }

    interface FlushableRegistryAware {
        void setFlushableRegistry(final FlushableRegistry flushableRegistry);
    }
}
