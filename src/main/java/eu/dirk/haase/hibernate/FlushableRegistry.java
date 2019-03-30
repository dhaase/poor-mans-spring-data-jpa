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

    boolean isCurrentInFlushSequence();

    interface UntilNowFlushable {

        void realFlush();

        void setUntilNowFlushableFunction(final Runnable untilNowFlushableFunction);

        static boolean flush(final Runnable untilNowFlushableFunction) {
            if (untilNowFlushableFunction != null) {
                untilNowFlushableFunction.run();
                return true;
            }
            return false;
        }
    }

    interface FlushableRegistryAware {
        void setFlushableRegistry(final FlushableRegistry flushableRegistry);
    }
}
