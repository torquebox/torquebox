/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.mc;

import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.kernel.plugins.registry.AbstractKernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryPlugin;
import org.jboss.logging.Logger;

/**
 * A kernel registry plugin which checks for CDI injectables.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:ajustin@redhat.com">Ales Justin</a>
 * @version $Revision: 72001 $
 */
public class CDIKernelRegistryPlugin implements KernelRegistryPlugin {
    private static final Logger log = Logger.getLogger( CDIKernelRegistryPlugin.class );

    public static final String CDI_DEPENDENCY_PREFIX = "cdi:";
    public static final String BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";

    private Hashtable<?, ?> environment;
    private BeanManager beanManager;

    public CDIKernelRegistryPlugin() {
    }

    @SuppressWarnings("rawtypes")
    public CDIKernelRegistryPlugin(Hashtable environment) {
        this.environment = environment;
    }

    public void create() throws NamingException {
        InitialContext context = new InitialContext( environment );
        try {
            this.beanManager = (BeanManager) context.lookup( BEAN_MANAGER_JNDI_NAME );
        } finally {
            context.close();
        }
    }

    public void destroy() throws NamingException {
    }

    public KernelRegistryEntry getEntry(Object name) {
        assert name != null : "name is null";

        String s = String.valueOf( name );
        if (!s.startsWith( CDI_DEPENDENCY_PREFIX ))
            return null;

        if (log.isTraceEnabled())
            log.trace( "get entry for " + name );

        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Type beanType = cl.loadClass( s );
            Set<Bean<?>> beans = this.beanManager.getBeans( beanType );
            
            if ( beans.isEmpty() ) {
                return null;
            }
            
            Bean<?> firstBean = beans.iterator().next();
            
            Object target = this.beanManager.getReference( firstBean, beanType, beanManager.createCreationalContext( firstBean ) );
            if (log.isTraceEnabled()) {
                log.trace( "found: " + target );
            }
            return new AbstractKernelRegistryEntry( name, target );
        } catch (RuntimeException e) {
            log.trace( "entry can't be resolved", e );
            throw e;
        } catch (ClassNotFoundException e) {
            log.error(  "Unable to find class: " + name );
            throw new RuntimeException(e);
        }
    }

    public void setEnvironment(Hashtable<?, ?> env) {
        this.environment = env;
    }
}
