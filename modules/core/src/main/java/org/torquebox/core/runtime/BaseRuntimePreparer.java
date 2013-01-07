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

package org.torquebox.core.runtime;

import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceRegistry;
import org.jruby.Ruby;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.component.ComponentRegistry;
import org.torquebox.core.util.JRubyConstants;
import org.torquebox.core.util.RuntimeHelper;

public class BaseRuntimePreparer implements RuntimePreparer {

    public BaseRuntimePreparer(RubyAppMetaData rubyAppMetaData) {
        this.rubyAppMetaData = rubyAppMetaData;
    }

    @Override
    public void prepareRuntime(Ruby ruby, String runtimeContext, ServiceRegistry serviceRegistry) throws Exception {
        if (rubyAppMetaData != null) {
            ruby.setCurrentDirectory( rubyAppMetaData.getRoot().getCanonicalPath() );
        }

        if ("1.6.3".equals( JRubyConstants.getVersion() ) ||
                "1.6.4".equals( JRubyConstants.getVersion() )) {
            log.debug( "Disabling POSIX ENV passthrough for " + runtimeContext + " runtime (TORQUE-497)" );
            StringBuffer env_fix = new StringBuffer();
            env_fix.append( "update_real_env_attr = org.jruby.RubyGlobal::StringOnlyRubyHash.java_class.declared_fields.find { |f| f.name == 'updateRealENV' }\n" );
            env_fix.append( "update_real_env_attr.accessible = true\n" );
            env_fix.append( "update_real_env_attr.set_value(ENV.to_java, false)\n" );
            ;
            RuntimeHelper.evalScriptlet( ruby, env_fix.toString() );
        }

        RuntimeHelper.require( ruby, "rubygems" );
        // torquebox-core may not be available if the application uses
        // Bundler and torquebox is not in their Gemfile
        RuntimeHelper.requireIfAvailable( ruby, "torquebox-core" );

        RuntimeHelper.require( ruby, "org/torquebox/core/runtime/thread_context_patch" );

        injectServiceRegistry( ruby, serviceRegistry );
        ComponentRegistry.createRegistryFor( ruby );
    }
    
    private void injectServiceRegistry(Ruby runtime, ServiceRegistry serviceRegistry) {
        // torquebox/service_registry is always available even if Bundler
        // is in use and torquebox isn't in the Gemfile because it gets
        // loaded before bundler is setup
        RuntimeHelper.require( runtime, "torquebox/service_registry" );
        RuntimeHelper.invokeClassMethod( runtime, "TorqueBox::ServiceRegistry", "service_registry=", new Object[] { serviceRegistry } );
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );

    protected RubyAppMetaData rubyAppMetaData;

}
