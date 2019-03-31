package eu.dirk.haase.hibernate;

import org.hibernate.SessionFactory;

import javax.persistence.EntityManagerFactory;

/**
 *
 */
public interface GlobalFlushables {

    /**
     * @return
     */
    static CurrentFlusher current() {
        return InternalGlobalFlushables.SINGLETON;
    }

    /**
     * Liefert den Zugriff auf {@link Initializer} um die {@link EntityManagerFactory}
     * sowie die {@link SessionFactory} mit der {@link FlushableRegistry} initialisieren
     * zu k&ouml;nnen.
     *
     * @return der Initialisierer um die {@link FlushableRegistry} zu initialisieren.
     */
    static Initializer init() {
        return InternalGlobalFlushables.SINGLETON;
    }

    /**
     *
     */
    interface CurrentFlusher {
        /**
         * @return
         */
        int flushAll();

        /**
         *
         * @return
         */
        boolean isCurrentlyInFlushSequence();
    }

    /**
     * Initialisiert die {@link EntityManagerFactory} sowie die {@link SessionFactory}
     * mit der {@link FlushableRegistry}
     */
    interface Initializer {

        /**
         * Initialisiert die {@link EntityManagerFactory} mit der {@link FlushableRegistry} sofern
         * die SessionFactory auch das Interface {@link FlushableRegistry.FlushableRegistryAware}
         * implementiert.
         *
         * @param entityManagerFactory die EntityManagerFactory initialisiert werden soll.
         */
        void initEntityManagerFactory(final EntityManagerFactory entityManagerFactory);

        /**
         * Initialisiert die {@link SessionFactory} mit der {@link FlushableRegistry} sofern die
         * SessionFactory auch das Interface {@link FlushableRegistry.FlushableRegistryAware}
         * implementiert.
         *
         * @param sessionFactory die SessionFactory initialisiert werden soll.
         */
        void initSessionFactory(final SessionFactory sessionFactory);
    }

    /**
     * Die interne Implementation der Interfaces {@link GlobalFlushables}, {@link CurrentFlusher}
     * und {@link Initializer}.
     */
    final class InternalGlobalFlushables implements GlobalFlushables, CurrentFlusher, Initializer {

        private final static InternalGlobalFlushables SINGLETON = new InternalGlobalFlushables();
        private final FlushableRegistry registry;

        private InternalGlobalFlushables() {
            this.registry = FlushableRegistry.newInstance();
        }

        @Override
        public int flushAll() {
            return this.registry.flushAllCurrent();
        }

        @Override
        public boolean isCurrentlyInFlushSequence() {
            return this.registry.isCurrentlyInFlushSequence();
        }

        @Override
        public void initEntityManagerFactory(final EntityManagerFactory entityManagerFactory) {
            if (entityManagerFactory instanceof FlushableRegistry.FlushableRegistryAware) {
                ((FlushableRegistry.FlushableRegistryAware) entityManagerFactory).setFlushableRegistry(this.registry);
            }
        }

        @Override
        public void initSessionFactory(final SessionFactory sessionFactory) {
            if (sessionFactory instanceof FlushableRegistry.FlushableRegistryAware) {
                ((FlushableRegistry.FlushableRegistryAware) sessionFactory).setFlushableRegistry(this.registry);
            }
        }

    }
}