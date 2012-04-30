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

package org.torquebox.web.rails;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.util.RuntimeHelper;
import org.torquebox.web.rack.RackMetaData;
import org.torquebox.web.rack.RackRuntimeInitializer;

public class RailsRuntimeInitializer extends RackRuntimeInitializer {

    private List<String> autoloadPaths = new ArrayList<String>();

    @SuppressWarnings("unused")
    private RailsMetaData railsAppMetaData;

    public RailsRuntimeInitializer(RubyAppMetaData rubyAppMetaData, RackMetaData rackAppMetaData, RailsMetaData railsAppMetaData) {
        super( rubyAppMetaData, rackAppMetaData );
        this.railsAppMetaData = railsAppMetaData;
    }

    public String getRailsEnv() {
        return getRackEnv();
    }

    public void addAutoloadPath(String path) {
        this.autoloadPaths.add( path );
    }

    public List<String> getAutoloadPaths() {
        return this.autoloadPaths;
    }

    @Override
    public void initialize(Ruby ruby, String runtimeContext) throws Exception {
        setRuntimeType( ruby, "rails" );
        super.initialize( ruby, runtimeContext );
       
        Logger logger = Logger.getLogger( getRubyAppMetaData().getApplicationName() );
        IRubyObject rubyLogger = JavaEmbedUtils.javaToRuby( ruby, logger );
        ruby.getGlobalVariables().set( "$JBOSS_RAILS_LOGGER", rubyLogger );

        File scriptLocation = new File( getApplicationRoot(), "<torquebox-bootstrap>-boot.rb" );
        makeAutoloadPathsAvailable( ruby );
        RuntimeHelper.executeScript( ruby, createBoot(), scriptLocation.getAbsolutePath() );
    }

    protected String createBoot() {
        StringBuffer bootScript = new StringBuffer();
        bootScript.append( "ENV['RAILS_ROOT']=RACK_ROOT\n" );
        bootScript.append( "ENV['RAILS_ENV']=RACK_ENV\n" );
        bootScript.append( "require %q(org/torquebox/web/rails/boot)\n" );
        return bootScript.toString();
    }

    protected void makeAutoloadPathsAvailable(Ruby ruby) {
        RubyModule object = ruby.getClassFromPath( "Object" );
        object.setConstant( "TORQUEBOX_RAILS_AUTOLOAD_PATHS", JavaEmbedUtils.javaToRuby( ruby, getAutoloadPaths() ) );
    }

}
