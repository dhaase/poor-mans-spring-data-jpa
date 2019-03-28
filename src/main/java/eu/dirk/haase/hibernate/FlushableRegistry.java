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
}
