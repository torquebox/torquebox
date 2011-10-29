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

package org.torquebox.web.rack;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
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
        return this.rubyAppMetaData.getEnvironmentName();
    }

    @Override
    public void initialize(Ruby ruby) throws Exception {
        RuntimeHelper.require(  ruby, "torquebox-web" );
        super.initialize( ruby );
        RuntimeHelper.evalScriptlet( ruby, getInitializerScript() );
    }

    /**
     * Create the initializer script.
     * 
     * @return The initializer script.
     */
    protected String getInitializerScript() {
        StringBuilder script = new StringBuilder();
        String appName = this.rubyAppMetaData.getApplicationName();
        String rackRootPath = this.rubyAppMetaData.getRootPath();
        String rackEnv = this.rubyAppMetaData.getEnvironmentName();
        String contextPath = this.rackAppMetaData.getContextPath();

        if (rackRootPath.endsWith( "/" )) {
            rackRootPath = rackRootPath.substring( 0, rackRootPath.length() - 1 );
        }

        if (!rackRootPath.startsWith( "vfs:/" )) {
            if (!rackRootPath.startsWith( "/" )) {
                rackRootPath = "/" + rackRootPath;
            }
        }

        script.append( "RACK_ROOT=%q(" + rackRootPath + ")\n" );
        script.append( "RACK_ENV=%q(" + rackEnv + ")\n" );
        script.append( "TORQUEBOX_APP_NAME=%q(" + appName + ")\n" );
        script.append( "TORQUEBOX_RACKUP_CONTEXT=%q(" + contextPath + ")\n" );
        script.append( "ENV['RACK_ROOT']=%q(" + rackRootPath + ")\n" );
        script.append( "ENV['RACK_ENV']=%q(" + rackEnv + ")\n" );
        script.append( "ENV['TORQUEBOX_APP_NAME']=%q(" + appName + ")\n" );

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
    private static final Logger log = Logger.getLogger( RackRuntimeInitializer.class );
    
    protected RackMetaData rackAppMetaData;

}
