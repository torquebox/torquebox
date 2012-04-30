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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.jboss.vfs.VFS;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.test.ruby.AbstractRubyTestCase;
import org.torquebox.web.rack.RackMetaData;

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
        File railsRoot = root( "/src/test/faux-rails2" );

        RailsRuntimeInitializer initializer = create( railsRoot, "development" );

        initializer.initialize( ruby, "web" );

        RubyClass objectClass = (RubyClass) ruby.getClassFromPath( "Object" );

        IRubyObject envConst = objectClass.getConstant( "ENV" );
        
        String rubyRailsRoot = (String) JavaEmbedUtils.invokeMethod( ruby, envConst, "[]", new Object[] { "RAILS_ROOT" }, String.class );
        assertNotNull( rubyRailsRoot );
        assertEquals( railsRoot.getCanonicalPath(), rubyRailsRoot );

        String rubyRailsEnv = (String) JavaEmbedUtils.invokeMethod( ruby, envConst, "[]", new Object[] { "RAILS_ENV" }, String.class );
        assertNotNull( rubyRailsEnv );
        assertEquals( "development", rubyRailsEnv );
    }


    @Test
    @Ignore
    // TODO move this to integ test?
    public void testOpenSSL_HMAC_digest() throws Exception {
        RailsRuntimeInitializer initializer = create( root( "/src/test/faux-rails2" ), "development" );

        initializer.initialize( ruby, "web" );

        String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
        ruby.evalScriptlet( script );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAutoloadPathsAvailableAsRubyConstant() throws Exception {
        RailsRuntimeInitializer initializer = create( root( "/src/test/faux-rails2" ), "development" );
        initializer.addAutoloadPath( "path1" );
        initializer.addAutoloadPath( "path2" );

        initializer.initialize( ruby, "web" );

        RubyModule object = ruby.getClassFromPath( "Object" );

        IRubyObject autoloadPaths = object.getConstant( "TORQUEBOX_RAILS_AUTOLOAD_PATHS" );
        assertNotNull( autoloadPaths );

        List<String> paths = (List<String>) JavaEmbedUtils.rubyToJava( autoloadPaths );
        assertTrue( paths.size() == 2 );
        assertTrue( paths.contains( "path1" ) );
        assertTrue( paths.contains( "path2" ) );
    }
    
    @Test
    public void testRails3() throws Exception {
        File railsRoot = root( "/src/test/faux-rails3" );
        RailsRuntimeInitializer initializer = create( railsRoot, "development" );

        initializer.initialize( ruby, "web" ); 
        
        RubyClass objectClass = (RubyClass) ruby.getObject();

        IRubyObject envConst = objectClass.getConstant( "ENV" );
        
        String rubyRailsRoot = (String) JavaEmbedUtils.invokeMethod( ruby, envConst, "[]", new Object[] { "RAILS_ROOT" }, String.class );
        assertNotNull( rubyRailsRoot );
        assertEquals( railsRoot.getCanonicalPath(), rubyRailsRoot );

        String rubyRailsEnv = (String) JavaEmbedUtils.invokeMethod( ruby, envConst, "[]", new Object[] { "RAILS_ENV" }, String.class );
        assertNotNull( rubyRailsEnv );
        assertEquals( "development", rubyRailsEnv );
    }

    private File root(String relativePath) {
        return new File( pwd(), relativePath );
    }
    
    private RailsRuntimeInitializer create(File root, String env) {
        RubyAppMetaData rubyAppMetaData = new RubyAppMetaData( "test-app");
        rubyAppMetaData.setRoot( root );
        rubyAppMetaData.setEnvironmentName( env );

        RackMetaData rackAppMetaData = new RackMetaData();
        RailsMetaData railsMetaData = new RailsMetaData();

        return new RailsRuntimeInitializer( rubyAppMetaData, rackAppMetaData, railsMetaData );
    }
}
