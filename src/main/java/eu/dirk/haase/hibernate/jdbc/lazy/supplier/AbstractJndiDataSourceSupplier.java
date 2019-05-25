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

    public void setBeanClassLoader(ClassLoader classLoader) {
        jndiObjectFactoryBean.setBeanClassLoader(classLoader);
    }

    public void setCache(boolean cache) {
        jndiObjectFactoryBean.setCache(cache);
    }

    public void setDefaultObject(Object defaultObject) {
        jndiObjectFactoryBean.setDefaultObject(defaultObject);
    }

    public void setExpectedType(Class<?> expectedType) {
        jndiObjectFactoryBean.setExpectedType(expectedType);
    }

    public void setExposeAccessContext(boolean exposeAccessContext) {
        jndiObjectFactoryBean.setExposeAccessContext(exposeAccessContext);
    }

    public void setJndiEnvironment(Properties jndiEnvironment) {
        jndiObjectFactoryBean.setJndiEnvironment(jndiEnvironment);
    }

    public void setJndiName(String jndiName) {
        setDescription(jndiName);
        jndiObjectFactoryBean.setJndiName(jndiName);
    }

    public void setJndiTemplate(JndiTemplate jndiTemplate) {
        jndiObjectFactoryBean.setJndiTemplate(jndiTemplate);
    }

    public void setLookupOnStartup(boolean lookupOnStartup) {
        jndiObjectFactoryBean.setLookupOnStartup(lookupOnStartup);
    }

    public void setProxyInterface(Class proxyInterface) {
        jndiObjectFactoryBean.setProxyInterface(proxyInterface);
    }

    public void setProxyInterfaces(Class[] proxyInterfaces) {
        jndiObjectFactoryBean.setProxyInterfaces(proxyInterfaces);
    }

    public void setResourceRef(boolean resourceRef) {
        jndiObjectFactoryBean.setResourceRef(resourceRef);
    }


}
