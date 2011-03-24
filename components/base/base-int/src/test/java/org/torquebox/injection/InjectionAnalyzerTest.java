package org.torquebox.injection;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.torquebox.injection.cdi.CDIInjectable;
import org.torquebox.injection.cdi.CDIInjectableHandler;
import org.torquebox.injection.jndi.JNDIInjectable;
import org.torquebox.injection.jndi.JNDIInjectableHandler;
import org.torquebox.injection.mc.MCBeanInjectable;
import org.torquebox.injection.mc.MCBeanInjectableHandler;

public class InjectionAnalyzerTest {
    
    private InjectionAnalyzer analyzer;
    private InjectableHandlerRegistry registry;

    protected String readScript(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream( name );
        InputStreamReader reader = new InputStreamReader( in );
        BufferedReader buffered = new BufferedReader( reader  );
       
        String line = null;
        StringBuilder script = new StringBuilder();
        
        while ( (line = buffered.readLine()) != null )  {
            script.append( line );
            script.append( "\n" );
        }
        
        return script.toString();
    }
    
    @Before
    public void setUpAnalyzer() {
        this.analyzer = new InjectionAnalyzer();
        this.registry = new InjectableHandlerRegistry();
        this.analyzer.setInjectableHandlerRegistry(  this.registry  );
        this.registry.addInjectableHandler( new MCBeanInjectableHandler() );
        this.registry.addInjectableHandler( new JNDIInjectableHandler() );
        this.registry.addInjectableHandler( new CDIInjectableHandler() );
    }
    
    @Test
    public void testAnalysis() throws Exception {
        String script = readScript( "injection.rb" );
        
        List<Injectable> injectables = analyzer.analyze( script.toString() );
        
        assertEquals( 3, injectables.size() );
        
        assertTrue( injectables.get( 0 ) instanceof MCBeanInjectable );
        assertEquals( "mc", injectables.get( 0 ).getType() );
        assertEquals( "jboss.whatever.Thing", injectables.get( 0 ).getName() );
        assertEquals( "jboss.whatever.Thing", injectables.get( 0 ).getKey() );
        
        assertTrue( injectables.get( 1 ) instanceof JNDIInjectable );
        assertEquals( "jndi", injectables.get( 1 ).getType() );
        assertEquals( "java:/comp/whatever", injectables.get( 1 ).getName() );
        assertEquals( "java:/comp/whatever", injectables.get( 1 ).getKey() );
        
        assertTrue( injectables.get( 2 ) instanceof CDIInjectable );
        assertEquals( "cdi", injectables.get( 2 ).getType() );
        assertEquals( "com.mycorp.mypackage.MyThing", injectables.get( 2 ).getName() );
        assertEquals( "Java::ComMycorpMypackage::MyThing", injectables.get( 2 ).getKey() );
    }
    
    @Test
    public void testAnalysisWithoutMarker() throws Exception {
        String script = readScript( "not_injection.rb" );
        
        List<Injectable> injectables = analyzer.analyze( script.toString() );
        
        assertTrue( injectables.isEmpty() );
    }

}
