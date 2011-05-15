package org.torquebox.cdi.injection;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.as.weld.WeldContainer;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.weld.literal.AnyLiteral;

public class CDIInjectableService implements Service<Object> {

    public CDIInjectableService(Class<?> type) {
        this.type = type;
    }

    @Override
    public Object getValue() throws IllegalStateException, IllegalArgumentException {
        return this.bean;
    }

    @Override
    public void start(StartContext context) throws StartException {
        WeldContainer container = this.weldContainerInjector.getValue();
        BeanManager beanManager = container.getBeanManager();

        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( type.getClassLoader() );
            Set<Bean<?>> beans = beanManager.getBeans( this.type, AnyLiteral.INSTANCE );

            System.err.println( "BEANS: " + beans );

            if (beans.size() > 1) {
                Set<Bean<?>> modifiableBeans = new HashSet<Bean<?>>();
                modifiableBeans.addAll( beans );
                // Ambiguous dependency may occur if a resource has subclasses
                // Therefore we remove those beans
                for (Iterator<Bean<?>> iterator = modifiableBeans.iterator(); iterator.hasNext();) {
                    Bean<?> bean = iterator.next();
                    if (!bean.getBeanClass().equals( this.type ) && !bean.isAlternative()) {
                        // remove Beans that have clazz in their type closure
                        // but
                        // not as a base class
                        iterator.remove();
                    }
                }
                beans = modifiableBeans;
            }

            Bean<?> bean = beanManager.resolve( beans );

            System.err.println( "RESOLVED: " + bean );
            CreationalContext<?> creationContext = beanManager.createCreationalContext( bean );
            this.bean = beanManager.getReference( bean, type, creationContext );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    public Injector<WeldContainer> getWeldContainerInjector() {
        return this.weldContainerInjector;
    }

    private InjectedValue<WeldContainer> weldContainerInjector = new InjectedValue<WeldContainer>();
    private Class<?> type;
    private Object bean;

}
