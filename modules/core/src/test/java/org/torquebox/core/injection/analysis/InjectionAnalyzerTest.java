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

package org.torquebox.core.injection.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.jruby.Ruby;
import org.jruby.RubyProc;
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

    protected Set<Injectable> analyzeScript(String name) throws Exception {
        return analyzeScript( name, Version.V1_8 );
    }

    protected Set<Injectable> analyzeScript(String name, Version version) throws Exception {
        InputStream script = openScript( name );
        analyzer.analyze( "testfile.rb", script, this.visitor, version );
        return this.visitor.getInjectables();
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
        Set<Injectable> injectables = analyzeScript( "injection.rb" );

        assertEquals( 2, injectables.size() );

        assertContains( injectables, ServiceInjectable.class, "service", "org.jboss.whatever.Thing", "org.jboss.whatever.Thing" );
        assertContains( injectables, JNDIInjectable.class, "jndi", "java:/comp/whatever", "java:/comp/whatever" );
    }

 
    @Test(expected = InvalidInjectionTypeException.class)
    public void testInvalidAnalysis() throws Exception {
        analyzeScript( "invalid_injection.rb" );
    }

    @Test
    public void test19CodeIn19Mode() throws Exception {
        Set<Injectable> injectables = analyzeScript( "injection_19.rb", Version.V1_9 );

        assertEquals( 3, injectables.size() );
    }

    @Test
    public void testInjectionInsideJRubyNonVisitableNodeIn19Mode() throws Exception {
        Set<Injectable> injectables = analyzeScript( "injection_19.rb", Version.V1_9 );

        assertEquals( 3, injectables.size() );

        assertContains( injectables, JNDIInjectable.class, "jndi", "java:/some/hidden/thing", "java:/some/hidden/thing" );

    }

    @Test
    public void test19CodeIn18Mode() throws Exception {
        Set<Injectable> injectables = analyzeScript( "injection_19.rb", Version.V1_8 );

        assertTrue( injectables.isEmpty() );
    }

    @Test
    public void testAnalysisWithoutMarker() throws Exception {
        Set<Injectable> injectables = analyzeScript( "not_injection.rb" );

        assertTrue( injectables.isEmpty() );
    }

    @Test
    public void testGenericAnalysis() throws Exception {
        Set<Injectable> injectables = analyzeScript( "generic_injection.rb" );

        assertEquals( 3, injectables.size() );
    }

    @Test(expected=IllegalInjectionException.class)
    public void testMultiArgIllegalAnalysis() throws Exception {
        analyzeScript( "multi_arg_illegal_injection.rb" );
    }

    @Test(expected=IllegalInjectionException.class)
    public void testIVarIllegalAnalysis() throws Exception {
        analyzeScript( "ivar_illegal_injection.rb" );
    }
    
    @Test
    public void testSplatIncludes() throws Exception {
        analyzeScript( "splat_injection.rb" );
    }

    
    @Test
    public void testProcAnalysis() throws Exception {
        Ruby ruby = Ruby.newInstance();
        
        RubyProc proc = (RubyProc) ruby.evalScriptlet( "Proc.new do |arg1, arg2|\n  inject('/queues/foo')\nend");
        this.visitor.assumeMarkerSeen();
        analyzer.analyze( proc, this.visitor );
        
        Set<Injectable> injectables = this.visitor.getInjectables();
        
        System.err.println( injectables );
    }
    
    protected void assertContains(Set<Injectable> actual, Class<?> injectableClass, String type, String name, String key) {
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
