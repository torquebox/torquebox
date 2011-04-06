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

package org.torquebox.rails.core;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.rack.core.RackRuntimeInitializer;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;

public class RailsRuntimeInitializer extends RackRuntimeInitializer {

    private List<String> autoloadPaths = new ArrayList<String>();

    @SuppressWarnings("unused")
    private RailsApplicationMetaData railsAppMetaData;

    public RailsRuntimeInitializer(RubyApplicationMetaData rubyAppMetaData, RackApplicationMetaData rackAppMetaData, RailsApplicationMetaData railsAppMetaData) {
        super( rubyAppMetaData, rackAppMetaData );
        this.railsAppMetaData = railsAppMetaData;
    }

    public VirtualFile getRailsRoot() {
        return getRackRoot();
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

    public void initialize(Ruby ruby) throws Exception {
        super.initialize( ruby );
        Logger logger = Logger.getLogger( getRailsRoot().toURL().toExternalForm() );
        IRubyObject rubyLogger = JavaEmbedUtils.javaToRuby( ruby, logger );
        ruby.getGlobalVariables().set( "$JBOSS_RAILS_LOGGER", rubyLogger );

        String scriptLocationBase = new URL( getRailsRoot().toURL(), "<torquebox-bootstrap>" ).toExternalForm();
        makeAutoloadPathsAvailable( ruby );
        ruby.executeScript( createBoot( getRailsRoot() ), scriptLocationBase + "-boot.rb" );
    }

    protected String createBoot(VirtualFile railsRoot) throws MalformedURLException, URISyntaxException {
        StringBuffer bootScript = new StringBuffer();
        bootScript.append( "ENV['RAILS_ROOT']=RACK_ROOT\n" );
        bootScript.append( "ENV['RAILS_ENV']=RACK_ENV\n" );
        bootScript.append( "require %q(org/torquebox/rails/core/boot)\n" );
        return bootScript.toString();
    }

    protected void makeAutoloadPathsAvailable(Ruby ruby) {
        RubyModule object = ruby.getClassFromPath( "Object" );
        object.setConstant( "TORQUEBOX_RAILS_AUTOLOAD_PATHS", JavaEmbedUtils.javaToRuby( ruby, getAutoloadPaths() ) );
    }

}
