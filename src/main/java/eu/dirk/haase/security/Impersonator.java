package eu.dirk.haase.security;

public interface Impersonator {

    String currentUser();

    void impersonate(final String runAsUser, final Runnable command);

    ImpersonationContext impersonate(final String runAsUser);

}
