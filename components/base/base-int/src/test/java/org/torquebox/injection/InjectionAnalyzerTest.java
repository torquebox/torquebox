package org.torquebox.injection;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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
    }
    
    @Test
    public void testAnalysis() throws Exception {
        String script = readScript( "injection.rb" );
        
        List<Injection> injections = analyzer.analyze( script.toString() );
        
        assertEquals( 2, injections.size() );
        
        assertEquals( "random", injections.get( 0 ).getSiteName() );
        assertTrue( injections.get( 0 ).getInjectable() instanceof MCBeanInjectable );
        assertEquals( "jboss.whatever.Thing", injections.get( 0 ).getInjectable().getName() );
        
        assertEquals( "something", injections.get( 1 ).getSiteName() );
        assertTrue( injections.get( 1 ).getInjectable() instanceof JNDIInjectable );
        assertEquals( "java:/comp/whatever", injections.get( 1 ).getInjectable().getName() );
        
    }

}
