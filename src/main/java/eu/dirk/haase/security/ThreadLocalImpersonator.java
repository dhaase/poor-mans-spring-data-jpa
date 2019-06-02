package eu.dirk.haase.security;

import java.util.Objects;
import java.util.function.Supplier;

public final class ThreadLocalImpersonator implements Impersonator {

    private final ThreadLocal<Context> currentContextThreadLocal;
    private final ThreadLocal<String> currentUserThreadLocal;
    private final Runnable flushAllRunnable;

    /**
     * Erzeugt einen neuen {@code Impersonator} aus einer Standard-Implementation.
     *
     * @param flushAllRunnable die Flush-Funktion um vorgelagerte potentielle Datenbank-
     *                         &Auml;nderungen anzuwendeen bevor der User gewechselt
     *                         wird.
     */
    public ThreadLocalImpersonator(final Runnable flushAllRunnable) {
        Objects.requireNonNull(flushAllRunnable, "Flush-All function can not be null");
        this.flushAllRunnable = flushAllRunnable;
        this.currentUserThreadLocal = new ThreadLocal<>();
        this.currentContextThreadLocal = new ThreadLocal<>();
    }

    @Override
    public void clear() {
        final Context context = this.currentContextThreadLocal.get();
        if (context != null) {
            // Alle inneren Kontexte werden automatisch
            // geschlossen.
            context.rootContext.close();
        }
        this.currentUserThreadLocal.remove();
        this.currentContextThreadLocal.remove();
    }

    @Override
    public Supplier<String> currentUserSupplier() {
        return this.currentUserThreadLocal::get;
    }

    @Override
    public Context impersonate(final String runAsUser) {
        return new Context(runAsUser);
    }

    @Override
    public void impersonate(final String runAsUser, final Runnable command) {
        Objects.requireNonNull(command, "Runnable-Command can not be null");
        try (final Context context = impersonate(runAsUser)) {
            context.run(command);
        }
    }

    private String init(final String runAsUser) {
        flushAllRunnable.run();
        final String currentUser = this.currentUserThreadLocal.get();
        this.currentUserThreadLocal.set(runAsUser);
        return currentUser;
    }

    private void reset(final String lastUser) {
        flushAllRunnable.run();
        if (lastUser == null) {
            // Oberste Ebene - keine anderer Context ist aktiv
            this.currentUserThreadLocal.remove();
        } else {
            // Innere Ebene - weitere uebergeordnete
            // Contexte sind aktiv:
            this.currentUserThreadLocal.set(lastUser);
        }
    }

    private final class Context implements ImpersonationContext {

        final String currentUser;
        final String lastUser;
        Context innerContext;
        Context rootContext;
        boolean isClosed;

        /**
         * Erzeugt einen neuen {@code ImpersonationContext}.
         *
         * @param runAsUser der User zum der ausf&uuml;hrenden Thread
         *                  ge&auml;ndert werden soll.
         */
        Context(final String runAsUser) {
            Objects.requireNonNull(runAsUser, "Impersonate-User can not be null");
            linkToOuterContext();
            this.lastUser = init(runAsUser);
            this.currentUser = runAsUser;
            this.isClosed = false;
        }

        @Override
        public void close() {
            if (innerContext != null) {
                innerContext.close();
            }
            if (!this.isClosed) {
                unlinkFromInnerContext();
                reset(this.lastUser);
            }
            this.isClosed = true;
        }

        @Override
        public String currentUser() {
            return this.currentUser;
        }

        @Override
        public boolean isClosed() {
            return isClosed;
        }

        @Override
        public String lastUser() {
            return this.lastUser;
        }

        private void linkToOuterContext() {
            final Context outerContext = ThreadLocalImpersonator.this.currentContextThreadLocal.get();
            if (outerContext == null) {
                // Oberste Ebene - keine anderer Context ist aktiv
                rootContext = this;
            } else {
                // Innere Ebene - weitere uebergeordnete
                // Contexte sind aktiv:
                outerContext.innerContext = this;
                rootContext = outerContext.rootContext;
            }
            ThreadLocalImpersonator.this.currentContextThreadLocal.set(this);
        }

        void run(final Runnable command) {
            command.run();
        }

        private void unlinkFromInnerContext() {
            innerContext = null;
            if (lastUser == null) {
                // Oberste Ebene - keine anderer Context ist aktiv
                ThreadLocalImpersonator.this.currentContextThreadLocal.remove();
            } else {
                // Innere Ebene - weitere uebergeordnete
                // Contexte sind aktiv:
                ThreadLocalImpersonator.this.currentContextThreadLocal.set(this);
            }
        }
    }

}
