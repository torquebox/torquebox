/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.cdi.injection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.as.weld.WeldStartService;
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
        WeldStartService startService = this.weldStartServiceInjector.getValue();
        BeanManager beanManager = startService.getBootstrap().getValue().getBeanManager();

        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( type.getClassLoader() );
            Set<Bean<?>> beans = beanManager.getBeans( this.type, AnyLiteral.INSTANCE );

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

            CreationalContext<?> creationContext = beanManager.createCreationalContext( bean );
            this.bean = beanManager.getReference( bean, type, creationContext );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    public Injector<WeldStartService> getWeldStartServiceInjector() {
        return this.weldStartServiceInjector;
    }

    private InjectedValue<WeldStartService> weldStartServiceInjector = new InjectedValue<WeldStartService>();
    private Class<?> type;
    private Object bean;

}
