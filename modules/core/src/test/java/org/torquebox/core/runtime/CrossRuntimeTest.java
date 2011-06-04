package org.torquebox.core.runtime;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

public class CrossRuntimeTest {
    
    private Ruby runtime1;
    private Ruby runtime2;

    @Before
    public void setUpRuntimes() {
        this.runtime1 = Ruby.newInstance();
        this.runtime2 = Ruby.newInstance();
    }
    
    @Test
    public void testCrossRuntimes() {
        this.runtime1.evalScriptlet( "require %q(org/torquebox/core/runtime/my_thing)" );
        this.runtime2.evalScriptlet( "require %q(org/torquebox/core/runtime/my_stuff)" );
        
        IRubyObject thing1 = this.runtime1.evalScriptlet( "MyThing.new( 'one' )" );
        IRubyObject thing2 = this.runtime2.evalScriptlet( "MyStuff.new( 'two' )" );
        
        System.err.println( thing1 );
        System.err.println( thing2 );
        
        IRubyObject result = (IRubyObject) JavaEmbedUtils.invokeMethod( thing1.getRuntime(), thing1, "receive", new Object[] { thing2 }, IRubyObject.class );
        
        System.err.println( result.getRuntime() == this.runtime1 ); // false!
        System.err.println( result.getRuntime() == this.runtime2 ); // true!
        
        System.err.print( result );
        
    }

}
