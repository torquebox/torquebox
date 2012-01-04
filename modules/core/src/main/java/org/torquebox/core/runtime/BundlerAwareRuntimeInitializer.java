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
 * {@link RuntimeInitializer} for Ruby Rack applications.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class BundlerAwareRuntimeInitializer implements RuntimeInitializer {

    public BundlerAwareRuntimeInitializer(RubyAppMetaData rubyAppMetaData) {
        this.rubyAppMetaData = rubyAppMetaData;
    }

    public File getApplicationRoot() {
        return this.rubyAppMetaData.getRoot();
    }

    @Override
    public void initialize(Ruby ruby) throws Exception {
        ruby.setCurrentDirectory( this.rubyAppMetaData.getRoot().getCanonicalPath() );
        
        File gemfile = new File( getApplicationRoot(), "Gemfile" );
        if (gemfile.exists()) {
            log.info(  "Setting up Bundler" );
            RuntimeHelper.evalScriptlet( ruby, "ENV['BUNDLE_GEMFILE']='" + gemfile.getAbsolutePath() +  "'" );
            RuntimeHelper.require( ruby, "bundler/setup" );
            RuntimeHelper.evalScriptlet( ruby, "ENV['BUNDLE_GEMFILE']=nil" );
        }
    }


    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( BundlerAwareRuntimeInitializer.class );

    protected RubyAppMetaData rubyAppMetaData;

}
