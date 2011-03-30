package org.torquebox.injection.cdi;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

public class CDIBridge {
    
    private static final Logger log = Logger.getLogger( CDIBridge.class );

    public CDIBridge(String applicationName) {
        this.applicationName = applicationName;
    }
    
    public synchronized BeanManager getBeanManager() throws NamingException {

        if (this.manager == null) {

            InitialContext context = new InitialContext();

            try {
                this.manager = (BeanManager) context.lookup( getBeanManagerName() );
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
    
    public String getBeanManagerName() {
        return "java:global/cdi/" + this.applicationName + "/" + this.applicationName + "/BeanManager";
    }

    //public static final String BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";
    //public static final String BEAN_MANAGER_JNDI_NAME = "java:global/cdi/services/services/BeanManager";
    private String applicationName;
    private BeanManager manager;
}
