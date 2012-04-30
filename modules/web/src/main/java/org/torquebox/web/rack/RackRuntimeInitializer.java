/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web.rack;

import java.io.IOException;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.runtime.BundlerAwareRuntimeInitializer;
import org.torquebox.core.runtime.RuntimeInitializer;
import org.torquebox.core.util.RuntimeHelper;

/**
 * {@link RuntimeInitializer} for Ruby Rack applications.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RackRuntimeInitializer extends BundlerAwareRuntimeInitializer {


    public RackRuntimeInitializer(RubyAppMetaData rubyAppMetaData, RackMetaData rackMetaData) {
        super( rubyAppMetaData );
        this.rackAppMetaData = rackMetaData;
    }

    public String getRackEnv() {
        return getRubyAppMetaData().getEnvironmentName();
    }

    @Override
    public void initialize(Ruby ruby, String runtimeContext) throws Exception {
        setRuntimeType( ruby, "rack" );
        RuntimeHelper.require(  ruby, "torquebox-web" );
        super.initialize( ruby, runtimeContext );
        RuntimeHelper.evalScriptlet( ruby, getInitializerScript() );
    }

    protected void setRuntimeType(Ruby ruby, String type) {
        RuntimeHelper.evalScriptlet( ruby, "ENV['TORQUEBOX_APP_TYPE'] ||= '" + type + "'" );
    }
    
    /**
     * Create the initializer script.
     * 
     * @return The initializer script.
     */
    protected String getInitializerScript() {
        StringBuilder script = new StringBuilder();
        String rackEnv = getRubyAppMetaData().getEnvironmentName();
        String contextPath = this.rackAppMetaData.getContextPath();
        String rackRootPath = null;
        
        try { 
            rackRootPath = getRubyAppMetaData().getRoot().getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (rackRootPath.endsWith( "/" )) {
            rackRootPath = rackRootPath.substring( 0, rackRootPath.length() - 1 );
        }
        
        script.append( "RACK_ROOT=%q(" + rackRootPath + ")\n" );
        script.append( "RACK_ENV=%q(" + rackEnv + ")\n" );
        script.append( "TORQUEBOX_RACKUP_CONTEXT=%q(" + contextPath + ")\n" );
        script.append( "ENV['RACK_ROOT']=%q(" + rackRootPath + ")\n" );
        script.append( "ENV['RACK_ENV']=%q(" + rackEnv + ")\n" );

        // only set if not root context
        if (contextPath != null && contextPath.length() > 1) { 
            // context path should always start with a "/"
            if (!contextPath.startsWith( "/" )) {
                contextPath = "/" + contextPath;
            }
            script.append( "ENV['RAILS_RELATIVE_URL_ROOT']=%q(" + contextPath + ")\n" );
            script.append( "ENV['RACK_BASE_URI']=%q(" + contextPath + ")\n" );
        }

        return script.toString();
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack" );
    
    protected RackMetaData rackAppMetaData;

}
