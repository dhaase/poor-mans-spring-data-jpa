package eu.dirk.haase.security;

import java.util.Objects;
import java.util.function.Supplier;

public final class ThreadLocalImpersonator implements Impersonator {

    private final ThreadLocal<Context> currentContextThreadLocal;
    private final ThreadLocal<String> currentUserThreadLocal;
    private final Runnable flushAllRunnable;

    public ThreadLocalImpersonator(final Runnable flushAllRunnable) {
        this.flushAllRunnable = flushAllRunnable;
        this.currentUserThreadLocal = new ThreadLocal<>();
        this.currentContextThreadLocal = new ThreadLocal<>();
    }

    @Override
    public void clear() {
        this.currentUserThreadLocal.remove();
        this.currentContextThreadLocal.remove();
    }

    @Override
    public Supplier<String> currentUserSupplier() {
        return () -> this.currentUserThreadLocal.get();
    }

    @Override
    public Context impersonate(final String runAsUser) {
        return new Context(runAsUser);
    }

    @Override
    public void impersonate(final String runAsUser, final Runnable command) {
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
            this.currentUserThreadLocal.set(lastUser);
        }
    }

    private final class Context implements ImpersonationContext {

        final String currentUser;
        final String lastUser;
        Context innerContext;
        boolean isClosed;

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
            if (outerContext != null) {
                outerContext.innerContext = this;
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
                ThreadLocalImpersonator.this.currentContextThreadLocal.set(this);
            }
        }
    }

}
