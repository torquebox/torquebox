package org.torquebox.core.injection.analysis;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.jruby.exceptions.RaiseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.injection.jndi.JNDIInjectable;
import org.torquebox.core.injection.jndi.JNDIInjectableHandler;
import org.torquebox.core.injection.msc.ServiceInjectable;
import org.torquebox.core.injection.msc.ServiceInjectableHandler;
import org.torquebox.core.runtime.RubyRuntimeMetaData.Version;

public class InjectionAnalyzerTest {

    private InjectionAnalyzer analyzer;
    private InjectableHandlerRegistry registry;
    private InjectionRubyByteCodeVisitor visitor;

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

    protected InputStream openScript(String name) throws IOException {
        return getClass().getResourceAsStream( name );
    }

    @Before
    public void setUpAnalyzer() {
        this.registry = new InjectableHandlerRegistry();
        this.analyzer = new InjectionAnalyzer( this.registry );
        this.registry.addInjectableHandler( new JNDIInjectableHandler() );
        this.registry.addInjectableHandler( new ServiceInjectableHandler() );

        this.visitor = new InjectionRubyByteCodeVisitor( analyzer );
    }

    @After
    public void tearDownAnalyzer() {
        this.analyzer.destroy();
    }

    @Test
    public void testAnalysis() throws Exception {
        InputStream script = openScript( "injection.rb" );
        analyzer.analyze( "testfile.rb", script, this.visitor, Version.V1_8 );
        Set<Injectable> injectables = this.visitor.getInjectables();

        assertEquals( 2, injectables.size() );

        assertContains( injectables, ServiceInjectable.class, "service", "org.jboss.whatever.Thing", "org.jboss.whatever.Thing" );
        assertContains( injectables, JNDIInjectable.class, "jndi", "java:/comp/whatever", "java:/comp/whatever" );
    }

 
    @Test(expected = InvalidInjectionTypeException.class)
    public void testInvalidAnalysis() throws Exception {
        InputStream script = openScript( "invalid_injection.rb" );
        analyzer.analyze( "testfile.rb", script, this.visitor, Version.V1_8 );
    }

    @Test
    public void test19CodeIn19Mode() throws Exception {
        String script = readScript( "injection_19.rb" );
        analyzer.analyze( "testfile.rb", script, this.visitor, Version.V1_9 );
        Set<Injectable> injectables = this.visitor.getInjectables();

        assertEquals( 3, injectables.size() );
    }

    @Test
    public void testInjectionInsideJRubyNonVisitableNodeIn19Mode() throws Exception {
        String script = readScript( "injection_19.rb" );

        analyzer.analyze( "testfile.rb", script, this.visitor, Version.V1_9 );
        Set<Injectable> injectables = this.visitor.getInjectables();

        assertEquals( 3, injectables.size() );

        assertContains( injectables, JNDIInjectable.class, "jndi", "java:/some/hidden/thing", "java:/some/hidden/thing" );

    }

    @Test(expected = RaiseException.class)
    public void test19CodeIn18Mode() throws Exception {
        String script = readScript( "injection_19.rb" );

        analyzer.analyze( "testfile.rb", script, this.visitor, Version.V1_8 );
        Set<Injectable> injectables = this.visitor.getInjectables();
    }

    @Test
    public void testAnalysisWithoutMarker() throws Exception {
        String script = readScript( "not_injection.rb" );

        analyzer.analyze( "testfile.rb", script, this.visitor, Version.V1_9 );
        Set<Injectable> injectables = this.visitor.getInjectables();

        assertTrue( injectables.isEmpty() );
    }

    @Test
    public void testGenericAnalysis() throws Exception {
        String script = readScript( "generic_injection.rb" );
        
        analyzer.analyze( "testfile.rb", script, this.visitor, Version.V1_9 );
        Set<Injectable> injectables = this.visitor.getInjectables();
        
        assertEquals( 3, injectables.size() );

        /*
        assertTrue( injectables.get( 0 ) instanceof MCBeanInjectable );
        assertEquals( "mc", injectables.get( 0 ).getType() );
        assertEquals(
                "jboss.web:service=WebServer", injectables.get( 0 ).getName() );
        assertEquals( "jboss.web:service=WebServer", injectables.get( 0
                ).getKey() );

        assertTrue( injectables.get( 1 ) instanceof JNDIInjectable );
        assertEquals( "jndi", injectables.get( 1 ).getType() );
        assertEquals(
                "java:/comp/whatever", injectables.get( 1 ).getName() );
        assertEquals(
                "java:/comp/whatever", injectables.get( 1 ).getKey() );

        assertTrue( injectables.get( 2 ) instanceof CDIInjectable );
        assertEquals( "cdi", injectables.get( 2 ).getType() );
        assertEquals(
                "com.mycorp.mypackage.MyThing", injectables.get( 2 ).getName() );
        assertEquals( "Java::ComMycorpMypackage::MyThing", injectables.get( 2
                ).getKey() );
                */
    }
    
    protected void assertContains(Set<Injectable> actual, Class injectableClass, String type, String name, String key) {
        int numFound = 0;

        for (Injectable each : actual) {
            if (injectableClass.isInstance( each )) {
                if (each.getType().equals( type )) {
                    if (each.getName().equals( name )) {
                        if (each.getKey().equals( key )) {
                            ++numFound;
                        }
                    }
                }
            }
        }

        if (numFound == 1) {
            return;
        }

        if (numFound > 1) {
            fail( "Too many found: " + injectableClass.getName() + "/" + type + "/" + name + "/" + key + ": " + numFound );
            return;
        }
        fail( "Expected but not found: " + injectableClass.getName() + "/" + type + "/" + name + "/" + key );

    }

    
    /*
     * @Test public void testPolishAnalysis() throws Exception { String script =
     * readScript( "injection-pl.rb" );
     * 
     * List<Injectable> injectables = analyzer.analyze( "testfile-pl.rb",
     * script, Version.V1_8 );
     * 
     * assertEquals( 3, injectables.size() );
     * 
     * assertTrue( injectables.get( 0 ) instanceof MCBeanInjectable );
     * assertEquals( "mc", injectables.get( 0 ).getType() ); assertEquals(
     * "jboss.whatever.Thing", injectables.get( 0 ).getName() ); assertEquals(
     * "jboss.whatever.Thing", injectables.get( 0 ).getKey() );
     * 
     * assertTrue( injectables.get( 1 ) instanceof JNDIInjectable );
     * assertEquals( "jndi", injectables.get( 1 ).getType() ); assertEquals(
     * "java:/comp/whatever", injectables.get( 1 ).getName() ); assertEquals(
     * "java:/comp/whatever", injectables.get( 1 ).getKey() );
     * 
     * assertTrue( injectables.get( 2 ) instanceof CDIInjectable );
     * assertEquals( "cdi", injectables.get( 2 ).getType() ); assertEquals(
     * "pl.mycorp.mypackage.MyThing", injectables.get( 2 ).getName() );
     * assertEquals( "Java::PlMycorpMypackage::MyThing", injectables.get( 2
     * ).getKey() ); }
     */

}
