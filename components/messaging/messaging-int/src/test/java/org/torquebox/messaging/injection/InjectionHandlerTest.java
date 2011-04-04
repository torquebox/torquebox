package org.torquebox.messaging.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.torquebox.injection.Injectable;
import org.torquebox.injection.InjectableHandlerRegistry;
import org.torquebox.injection.InjectionAnalyzer;
import org.torquebox.injection.cdi.CDIInjectable;
import org.torquebox.injection.cdi.CDIInjectableHandler;
import org.torquebox.injection.jndi.JNDIInjectable;
import org.torquebox.injection.jndi.JNDIInjectableHandler;
import org.torquebox.injection.mc.MCBeanInjectable;
import org.torquebox.injection.mc.MCBeanInjectableHandler;
import org.torquebox.interp.metadata.RubyRuntimeMetaData.Version;

public class InjectionHandlerTest {

    private InjectionAnalyzer analyzer;
    private InjectableHandlerRegistry registry;

    protected String readScript(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream( name );
        InputStreamReader reader = new InputStreamReader( in );
        BufferedReader buffered = new BufferedReader( reader );

        String line = null;
        StringBuilder script = new StringBuilder();

        while ((line = buffered.readLine()) != null) {
            script.append( line );
            script.append( "\n" );
        }

        return script.toString();
    }

    @Before
    public void setUpAnalyzer() {
        this.analyzer = new InjectionAnalyzer();
        this.registry = new InjectableHandlerRegistry();
        this.analyzer.setInjectableHandlerRegistry( this.registry );
        this.registry.addInjectableHandler( new MCBeanInjectableHandler() );
        this.registry.addInjectableHandler( new JNDIInjectableHandler() );
        this.registry.addInjectableHandler( new CDIInjectableHandler() );
        this.registry.addInjectableHandler( new DestinationInjectableHandler( "queue" ) );
        this.registry.addInjectableHandler( new DestinationInjectableHandler( "topic" ) );
    }

    @Test
    public void testGenericAnalysis() throws Exception {
        String script = readScript( "generic_injection.rb" );
        List<Injectable> injectables = analyzer.analyze( "generic_injection.rb", script.toString(), Version.V1_8 );

        assertEquals( 5, injectables.size() );

        assertTrue( injectables.get( 0 ) instanceof MCBeanInjectable );
        assertEquals( "mc", injectables.get( 0 ).getType() );
        assertEquals( "jboss.web:service=WebServer", injectables.get( 0 ).getName() );
        assertEquals( "jboss.web:service=WebServer", injectables.get( 0 ).getKey() );

        assertTrue( injectables.get( 1 ) instanceof JNDIInjectable );
        assertEquals( "jndi", injectables.get( 1 ).getType() );
        assertEquals( "java:/comp/whatever", injectables.get( 1 ).getName() );
        assertEquals( "java:/comp/whatever", injectables.get( 1 ).getKey() );

        assertTrue( injectables.get( 2 ) instanceof CDIInjectable );
        assertEquals( "cdi", injectables.get( 2 ).getType() );
        assertEquals( "com.mycorp.mypackage.MyThing", injectables.get( 2 ).getName() );
        assertEquals( "Java::ComMycorpMypackage::MyThing", injectables.get( 2 ).getKey() );
    }

}
