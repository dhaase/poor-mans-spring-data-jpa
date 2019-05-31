package eu.dirk.haase.security;

public interface ImpersonationContext extends AutoCloseable {
    void close();

    String currentUser();

    boolean isClosed();
}
