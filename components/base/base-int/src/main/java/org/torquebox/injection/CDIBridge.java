package org.torquebox.injection;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CDIBridge {

    public CDIBridge() {

    }

    public CDIBridge(Hashtable<?, ?> environment) {
        this.environment = environment;
    }

    public void setEnvironment(Hashtable<?, ?> environment) {
        this.environment = environment;
    }

    public Hashtable<?, ?> getEnvironment() {
        return this.environment;
    }

    public synchronized BeanManager getBeanManager() throws NamingException {

        if (this.manager == null) {

            InitialContext context = new InitialContext( this.environment );

            try {
                this.manager = (BeanManager) context.lookup( BEAN_MANAGER_JNDI_NAME );
            } finally {
                context.close();
            }
        }
        return this.manager;
    }

    public Object get(String typeName) throws ClassNotFoundException, NamingException {
        Type type = Thread.currentThread().getContextClassLoader().loadClass( typeName );

        Set<Bean<?>> beans = getBeanManager().getBeans( type );

        if (beans.size() > 1) {
            Set<Bean<?>> modifiableBeans = new HashSet<Bean<?>>();
            modifiableBeans.addAll( beans );
            // Ambiguous dependency may occur if a resource has subclasses
            // Therefore we remove those beans
            for (Iterator<Bean<?>> iterator = modifiableBeans.iterator(); iterator.hasNext();) {
                Bean<?> bean = iterator.next();
                if (!bean.getBeanClass().equals( type ) && !bean.isAlternative()) {
                    // remove Beans that have clazz in their type closure but
                    // not as a base class
                    iterator.remove();
                }
            }
            beans = modifiableBeans;
        }

        Bean<?> bean = manager.resolve( beans );
        CreationalContext<?> context = manager.createCreationalContext( bean );
        return manager.getReference( bean, type, context );
    }

    public static final String BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";
    private Hashtable<?, ?> environment;
    private BeanManager manager;
}
