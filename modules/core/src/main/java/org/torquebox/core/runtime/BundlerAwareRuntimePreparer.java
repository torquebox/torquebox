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
import org.jboss.msc.service.ServiceRegistry;
import org.jruby.Ruby;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.util.RuntimeHelper;

public class BundlerAwareRuntimePreparer extends BaseRuntimePreparer {

    public BundlerAwareRuntimePreparer(RubyAppMetaData rubyAppMetaData) {
        this.rubyAppMetaData = rubyAppMetaData;
    }

    @Override
    public void prepareRuntime(Ruby ruby, String runtimeContext, ServiceRegistry serviceRegistry) throws Exception {
        // Ensure various TorqueBox bits are available even if torquebox
        // isn't in the Gemfile. Don't directly require torquebox-core
        // to avoid constant already initialized errors later on
        RuntimeHelper.require( ruby, "rubygems" );
        RuntimeHelper.require( ruby, "torquebox/service_registry" );
        RuntimeHelper.require( ruby, "torquebox/component_manager" );
        RuntimeHelper.require( ruby, "torquebox/injectors" );
        RuntimeHelper.require( ruby, "torquebox/logger" );

        File gemfile = new File( rubyAppMetaData.getRoot(), "Gemfile" );
        if (gemfile.exists()) {
            log.info(  "Setting up Bundler" );
            RuntimeHelper.evalScriptlet( ruby, "ENV['BUNDLE_GEMFILE']='" + gemfile.getAbsolutePath() +  "'" );
            RuntimeHelper.require( ruby, "bundler/setup" );
            RuntimeHelper.evalScriptlet( ruby, "ENV['BUNDLE_GEMFILE']=nil" );
            RuntimeHelper.require( ruby, "jruby" );
            RuntimeHelper.evalScriptlet( ruby, "JRuby.runtime.load_service.addLoadedFeature( 'torquebox/service_registry' )" );
        }
        super.prepareRuntime( ruby, runtimeContext, serviceRegistry );
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );

    private RubyAppMetaData rubyAppMetaData;

}
