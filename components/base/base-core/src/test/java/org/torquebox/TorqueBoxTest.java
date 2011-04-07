package org.torquebox;

import static org.junit.Assert.*;

import org.jruby.Ruby;
import org.junit.Test;

public class TorqueBoxTest {
    
    @Test
    public void testGlobalRuby() throws Exception {
        TorqueBox torquebox = new TorqueBox();
        torquebox.setGemPath( System.getProperty( "gem.path" ) );
        torquebox.create();
        
        Ruby ruby = torquebox.getGlobalRuntime();
        
        assertNotNull( ruby );
        assertEquals( "" + ruby.hashCode(), torquebox.getGlobalRuntimeName() );
        assertEquals( "TorqueBox::Kernel", torquebox.evaluate(  "TorqueBox::Kernel"  ).toString() );
    }

}
