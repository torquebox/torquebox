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

package org.torquebox.core.runtime;

import java.io.File;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.util.RuntimeHelper;

/**
 * {@link RuntimeInitializer} for Ruby applications.
 * 
 */
public class BaseRuntimeInitializer implements RuntimeInitializer {


    public BaseRuntimeInitializer(RubyAppMetaData rubyAppMetaData) {
        this.rubyAppMetaData = rubyAppMetaData;
    }

    @Override
    public void initialize(Ruby ruby) throws Exception {
        String appName = this.rubyAppMetaData.getApplicationName();
        ruby.setCurrentDirectory( getApplicationRoot().getCanonicalPath() );

        StringBuilder script = new StringBuilder();
        script.append( "TORQUEBOX_APP_NAME=%q(" + appName + ")\n" );
        script.append( "ENV['TORQUEBOX_APP_NAME']=%q(" + appName + ")\n" );
        
        RuntimeHelper.evalScriptlet( ruby, script.toString() );
        
        if ( this.rubyAppMetaData.getTorqueBoxInit() != null ) {
        	RuntimeHelper.call(ruby, this.rubyAppMetaData.getTorqueBoxInit(), "call", null);
        }
        try {
            RuntimeHelper.evalScriptlet( ruby, "require %q(torquebox_init)" );
        } catch (Throwable t) {
            // We can do this quietly since, torquebox_init.rb is not required
        	// But people get afeared of errors, and evalScriptlet generates one.
        	// So, let's clarify.
        	log.warn("No torquebox_init.rb found. That's just fine. Moving on...");
        }
    }

    public RubyAppMetaData getRubyAppMetaData() {
        return rubyAppMetaData;
    }

    public File getApplicationRoot() {
	    return rubyAppMetaData.getRoot();
	}
    
	@SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );
    
    private RubyAppMetaData rubyAppMetaData;
    
}
