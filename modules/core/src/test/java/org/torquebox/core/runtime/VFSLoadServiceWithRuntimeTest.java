package org.torquebox.core.runtime;

import static org.junit.Assert.*;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

public class VFSLoadServiceWithRuntimeTest {
    
    private VFSLoadService loadService;
    private Ruby ruby;

    @Before
    public void setUp() throws Exception {
        RubyRuntimeFactory factory = new RubyRuntimeFactory();
        factory.setUseJRubyHomeEnvVar( false );
        this.ruby = factory.createInstance( "test" );
        this.loadService = (VFSLoadService) this.ruby.getLoadService();
    }

    @Test
    public void testRubygemsLoadable() throws Exception {
        this.loadService.require( "rubygems" );
        IRubyObject result = this.ruby.evalScriptlet(  "Gem"  );
        assertNotNull( result );
    }

}
