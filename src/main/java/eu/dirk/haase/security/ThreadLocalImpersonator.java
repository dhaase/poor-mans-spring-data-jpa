package eu.dirk.haase.security;

import eu.dirk.haase.hibernate.GlobalFlushables;

public final class ThreadLocalImpersonator implements Impersonator {

    private final ThreadLocal<String> currentUserThreadLocal;

    public ThreadLocalImpersonator() {
        this.currentUserThreadLocal = new ThreadLocal<>();
    }

    @Override
    public String currentUser() {
        return this.currentUserThreadLocal.get();
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
        GlobalFlushables.current().flushAll();
        final String currentUser = this.currentUserThreadLocal.get();
        this.currentUserThreadLocal.set(runAsUser);
        return currentUser;
    }

    private void reset(final String currentUser) {
        GlobalFlushables.current().flushAll();
        if (currentUser == null) {
            this.currentUserThreadLocal.remove();
        } else {
            this.currentUserThreadLocal.set(currentUser);
        }
    }

    private final class Context implements ImpersonationContext {

        final String currentUser;
        boolean isClosed;

        Context(final String runAsUser) {
            this.isClosed = false;
            this.currentUser = init(runAsUser);
        }

        @Override
        public void close() {
            if (!this.isClosed) {
                reset(this.currentUser);
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

        void run(final Runnable command) {
            command.run();
        }
    }

}
