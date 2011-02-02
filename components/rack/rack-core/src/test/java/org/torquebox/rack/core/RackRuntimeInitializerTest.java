package org.torquebox.rack.core;

import static org.junit.Assert.*;

import org.jboss.vfs.VFS;
import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RackRuntimeInitializerTest extends AbstractRubyTestCase {

    @Test
    public void testInitializer() throws Exception {
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
        rubyAppMetaData.setRoot( VFS.getChild( "/myapp" ) );
        rubyAppMetaData.setEnvironmentName( "test" );
        rackAppMetaData.setContextPath( "/mycontext" );
        rackAppMetaData.setRackApplicationName( "app_name" );

        RackRuntimeInitializer initializer = new RackRuntimeInitializer(rubyAppMetaData, rackAppMetaData);

        Ruby ruby = createRuby();
        initializer.initialize( ruby );

        String rackRoot = (String) ruby.evalScriptlet( "RACK_ROOT" ).toJava(String.class);
        assertEquals( "vfs:" + vfsAbsolutePrefix() + "/myapp", rackRoot );

        String rackEnv = (String) ruby.evalScriptlet( "RACK_ENV" ).toJava(String.class);
        assertEquals( "test", rackEnv );

        String pwd = (String) ruby.evalScriptlet( "Dir.pwd" ).toJava(String.class);
        assertEquals( absolutePrefix() + "/myapp", pwd );

        String baseUri = (String) ruby.evalScriptlet( "ENV['RACK_BASE_URI']" ).toJava(String.class);
        assertEquals( "/mycontext", baseUri );

        String appName = (String) ruby.evalScriptlet( "ENV['TORQUEBOX_APP_NAME']").toJava(String.class);
        assertEquals( "app_name", appName);

        appName = (String) ruby.evalScriptlet( "TORQUEBOX_APP_NAME").toJava(String.class);
        assertEquals( "app_name", appName);

    }
}
