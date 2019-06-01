package eu.dirk.haase.security;

import java.util.function.Supplier;

public interface Impersonator {

    Supplier<String> currentUserSupplier();

    void impersonate(final String runAsUser, final Runnable command);

    ImpersonationContext impersonate(final String runAsUser);

}
