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

package org.torquebox.interp.core;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class ManagedComponentResolver implements RubyComponentResolver {

    private String componentName;
    private boolean alwaysReload;
    private static final Logger log = Logger.getLogger( ManagedComponentResolver.class );

    public ManagedComponentResolver() {
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public boolean isAlwaysReload() {
        return alwaysReload;
    }

    public void setAlwaysReload(boolean alwaysReload) {
        this.alwaysReload = alwaysReload;
    }

    @Override
    public IRubyObject resolve(Ruby ruby) throws Exception {
        log.debug( "resolve(" + ruby + ")" );
        synchronized (ruby) {
            log.debug( "Got exclusive access: " + ruby );
            ruby.getLoadService().require( "org/torquebox/interp/core/component_manager" );
            RubyClass managerClass = (RubyClass) ruby.getClassFromPath( "TorqueBox::ComponentManager" );
            log.debug( "Got manager: " + managerClass );
            IRubyObject component = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, managerClass, "lookup_component", new Object[] { this.componentName },
                    IRubyObject.class );
            log.debug( "Looked up component: " + component );

            if (isAlwaysReload() || component == null || component.isNil()) {
                component = createComponent( ruby );
                log.debug( "Created component: " + component );
                if (component != null) {
                    JavaEmbedUtils.invokeMethod( ruby, managerClass, "register_component", new Object[] { this.componentName, component }, void.class );
                    log.debug( "Registered component: " + component );
                }
                if (isAlwaysReload()) {
                    ruby.evalScriptlet( "Dispatcher.cleanup_application if defined?(Dispatcher)" );
                    log.debug( "Reloaded ruby: " + ruby );
                }
            }

            return component;
        }
    }

    protected abstract IRubyObject createComponent(Ruby ruby) throws Exception;

}
