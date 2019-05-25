package eu.dirk.haase.hibernate.jdbc.lazy.supplier;

import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import javax.sql.CommonDataSource;
import java.util.Properties;
import java.util.function.Supplier;

abstract class AbstractJndiDataSourceSupplier<T extends CommonDataSource> extends AbstractDataSourceSupplier<T> implements Supplier<T> {
    private final JndiObjectFactoryBean jndiObjectFactoryBean;

    AbstractJndiDataSourceSupplier(Class proxyInterface) {
        super(new Class[]{proxyInterface});
        jndiObjectFactoryBean = new JndiObjectFactoryBean();
        setProxyInterface(proxyInterface);
        setLookupOnStartup(false);
    }

    AbstractJndiDataSourceSupplier(Class[] proxyInterfaces) {
        super(proxyInterfaces);
        jndiObjectFactoryBean = new JndiObjectFactoryBean();
        setProxyInterfaces(proxyInterfaces);
        setLookupOnStartup(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    final T getInternal() {
        if (!isInitialized()) {
            try {
                this.jndiObjectFactoryBean.afterPropertiesSet();
            } catch (NamingException ex) {
                throw new IllegalStateException(ex.toString(), ex);
            }
        }
        return (T) this.jndiObjectFactoryBean.getObject();
    }

    @Override
    public final boolean isProxy() {
        return true;
    }

    public final void setBeanClassLoader(ClassLoader classLoader) {
        jndiObjectFactoryBean.setBeanClassLoader(classLoader);
    }

    public final void setCache(boolean cache) {
        jndiObjectFactoryBean.setCache(cache);
    }

    public final void setDefaultObject(Object defaultObject) {
        jndiObjectFactoryBean.setDefaultObject(defaultObject);
    }

    public final void setExpectedType(Class<?> expectedType) {
        jndiObjectFactoryBean.setExpectedType(expectedType);
    }

    public final void setExposeAccessContext(boolean exposeAccessContext) {
        jndiObjectFactoryBean.setExposeAccessContext(exposeAccessContext);
    }

    public final void setJndiEnvironment(Properties jndiEnvironment) {
        jndiObjectFactoryBean.setJndiEnvironment(jndiEnvironment);
    }

    public final void setJndiName(String jndiName) {
        if (getDescription() == null) {
            setDescription(jndiName);
        }
        jndiObjectFactoryBean.setJndiName(jndiName);
    }

    public final void setJndiTemplate(JndiTemplate jndiTemplate) {
        jndiObjectFactoryBean.setJndiTemplate(jndiTemplate);
    }

    private void setLookupOnStartup(boolean lookupOnStartup) {
        jndiObjectFactoryBean.setLookupOnStartup(lookupOnStartup);
    }

    private void setProxyInterface(Class proxyInterface) {
        jndiObjectFactoryBean.setProxyInterface(proxyInterface);
    }

    private void setProxyInterfaces(Class[] proxyInterfaces) {
        jndiObjectFactoryBean.setProxyInterfaces(proxyInterfaces);
    }

    public final void setResourceRef(boolean resourceRef) {
        jndiObjectFactoryBean.setResourceRef(resourceRef);
    }


}
