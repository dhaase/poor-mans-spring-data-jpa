package eu.dirk.haase.security;

public interface ImpersonationContext extends AutoCloseable {
    void close();

    String lastUser();

    String currentUser();

    boolean isClosed();
}
