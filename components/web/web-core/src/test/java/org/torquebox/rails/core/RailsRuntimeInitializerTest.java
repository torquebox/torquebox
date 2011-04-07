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

import static org.junit.Assert.*;

import java.util.List;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RailsRuntimeInitializerTest extends AbstractRubyTestCase {

    private Ruby ruby;

    @Before
    public void setUp() throws Exception {
        Class.forName( VFS.class.getName() );
        ruby = createRuby();
    }

    @After
    public void tearDown() throws Exception {
        ruby.tearDown( false );
        ruby = null;
        System.gc();
    }

    @Test
    public void testInitializeWithGems() throws Exception {
        String railsRootStr = pwd() + "/src/test/rails/ballast";

        railsRootStr = railsRootStr.replaceAll( "\\\\", "/" );
        String vfsRailsRootStr = toVfsPath( railsRootStr );

        VirtualFile railsRoot = VFS.getChild( railsRootStr );

        RailsRuntimeInitializer initializer = create( railsRoot, "development" );

        initializer.initialize( ruby );

        RubyClass objectClass = (RubyClass) ruby.getClassFromPath( "Object" );

        IRubyObject rubyRailsRoot = objectClass.getConstant( "RAILS_ROOT" );
        assertNotNull( rubyRailsRoot );
        assertNotNil( rubyRailsRoot );

        assertEquals( vfsRailsRootStr, rubyRailsRoot.toString() );

        IRubyObject rubyRailsEnv = objectClass.getConstant( "RAILS_ENV" );
        assertNotNull( rubyRailsEnv );
        assertNotNil( rubyRailsEnv );
        assertEquals( "development", rubyRailsEnv.toString() );
    }

    @Test(expected = RaiseException.class)
    public void testUnknownModelsAreCorrectlyIdentified() throws Exception {
        String railsRootStr = System.getProperty( "user.dir" ) + "/src/test/rails/ballast";
        VirtualFile railsRoot = VFS.getChild( railsRootStr );

        RailsRuntimeInitializer initializer = create( railsRoot, "development" );

        initializer.initialize( ruby );

        String script = "class Foo; def self.fetch(); NoSuchModel; end; end; Foo.fetch";
        ruby.evalScriptlet( script );
    }

    @Test
    public void testModelsAreLoadable() throws Exception {
        String railsRootStr = System.getProperty( "user.dir" ) + "/src/test/rails/ballast";
        VirtualFile railsRoot = VFS.getChild( railsRootStr );

        RailsRuntimeInitializer initializer = create( railsRoot, "development" );

        initializer.initialize( ruby );

        RubyModule bookModel = ruby.getClassFromPath( "Book" );
        assertNotNil( bookModel );
    }

    @Test
    public void testOpenSSL_HMAC_digest() throws Exception {
        String railsRootStr = System.getProperty( "user.dir" ) + "/src/test/rails/ballast";
        VirtualFile railsRoot = VFS.getChild( railsRootStr );
        RailsRuntimeInitializer initializer = create( railsRoot, "development" );

        initializer.initialize( ruby );

        String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
        ruby.evalScriptlet( script );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAutoloadPathsAvailableAsRubyConstant() throws Exception {
        String path = System.getProperty( "user.dir" ) + "/src/test/rails/ballast";
        VirtualFile root = VFS.getChild( path );

        RailsRuntimeInitializer initializer = create( root, "development" );
        initializer.addAutoloadPath( "path1" );
        initializer.addAutoloadPath( "path2" );

        initializer.initialize( ruby );

        RubyModule object = ruby.getClassFromPath( "Object" );

        IRubyObject autoloadPaths = object.getConstant( "TORQUEBOX_RAILS_AUTOLOAD_PATHS" );
        assertNotNull( autoloadPaths );

        List<String> paths = (List<String>) JavaEmbedUtils.rubyToJava( autoloadPaths );
        assertTrue( paths.size() == 2 );
        assertTrue( paths.contains( "path1" ) );
        assertTrue( paths.contains( "path2" ) );

        String load_paths = "" + ruby.evalScriptlet( "ActiveSupport::Dependencies.load_paths.join(',')" );
        assertTrue( load_paths.endsWith( ",path1,path2" ) );
    }
    
    @Test
    public void testRails3() throws Exception {
        String railsRootStr = System.getProperty( "user.dir" ) + "/src/test/rails/ballast3";
        VirtualFile railsRoot = VFS.getChild( railsRootStr );
        RailsRuntimeInitializer initializer = create( railsRoot, "development" );

        initializer.initialize( ruby ); 
    
        assertEquals( "vfs:" + railsRoot.getPathName(), ruby.evalScriptlet( "Rails.root" ).toString() );
    }

    private RailsRuntimeInitializer create(VirtualFile root, String env) {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        rubyAppMetaData.setApplicationName( "app_name" );
        rubyAppMetaData.setRoot( root );
        rubyAppMetaData.setEnvironmentName( env );

        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
        RailsApplicationMetaData railsMetaData = new RailsApplicationMetaData();

        return new RailsRuntimeInitializer( rubyAppMetaData, rackAppMetaData, railsMetaData );
    }
}
