package eu.dirk.haase.hibernate.jdbc;

import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.jdbc.Work;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;

abstract class AbstractHibernateSessionHandler<T> implements InvocationHandler, HibernateSessionLinker {


    final T delegate;
    Reference<IHibernateConnection> connectionReference;
    Reference<T> hibernateReference;
    boolean isClosed;
    boolean isHibernateConnection;
    Reference<HibernateSessionLinker> linker;

    AbstractHibernateSessionHandler(final T delegate) {
        this.isClosed = false;
        this.delegate = delegate;
        this.linker = refreshLinker();
        doWork(this.delegate, (c) -> determineHibernateConnection(c));
    }

    Reference<HibernateSessionLinker> refreshLinker() {
        if ((this.linker == null) || (this.linker.get() == null)) {
            return new WeakReference<>(this);
        } else {
            return this.linker;
        }
    }

    void becomeCurrentLinker(final ThreadLocal<Reference<HibernateSessionLinker>> threadLocalSession) {
        final Reference<HibernateSessionLinker> lastLinker = threadLocalSession.get();
        if (lastLinker != this.linker) {
            becomeObsoleteLinker(this.linker);
            becomeObsoleteLinker(lastLinker);
        }
        this.linker = refreshLinker();
        threadLocalSession.set(this.linker);
    }

    void becomeObsoleteLinker(final Reference<HibernateSessionLinker> lastLinker) {
        if (lastLinker != null) {
            lastLinker.clear();
        }
    }

    private void determineHibernateConnection(final Connection connection) {
        try {
            this.isHibernateConnection = connection.isWrapperFor(HibernateConnection.class);
        } catch (SQLException ex) {
            // Manche Treiber loesen eine Exception aus
            // dabei ist die Frage doch leicht zu beantworten:
            this.isHibernateConnection = false;
        }
    }

    abstract void doWork(final T delegate, final Work work) throws HibernateException;

    void unlinkHibernate() {
        if (this.isHibernateConnection) {
            if (this.connectionReference != null) {
                this.connectionReference.clear();
            }
            if (this.hibernateReference != null) {
                this.hibernateReference.clear();
            }
        }
    }

}
