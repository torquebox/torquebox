package org.torquebox.web.component;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.web.rack.RackEnvironment;

public class RackApplicationComponentTest {
    
    private Ruby ruby;
    private ComponentResolver resolver;
    private IRubyObject rackApp;

    @Before
    public void setUp() {
        this.ruby = Ruby.newInstance();
        
        this.ruby.getLoadService().require(  "org/torquebox/web/component/mock_app" );
        RubyModule mockAppClass = this.ruby.getClassFromPath( "MockApp" );
        this.rackApp = (IRubyObject) JavaEmbedUtils.invokeMethod(  this.ruby, mockAppClass, "new", null, IRubyObject.class );
        
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void testCall() throws Exception {
        RackApplicationComponent component = new RackApplicationComponent( this.rackApp );
        RackEnvironment env = mock( RackEnvironment.class );
        RubyHash envHash = RubyHash.newHash( this.ruby );
        when(env.getEnv()).thenReturn( envHash );
        
        Object response = component.call( env );
        
        assertNotNull( response );
        
        RubyHash internalEnvHash = (RubyHash) JavaEmbedUtils.invokeMethod( this.ruby, this.rackApp, "env", null, RubyHash.class );
        
        assertSame( envHash, internalEnvHash );
        
        
    }

}
