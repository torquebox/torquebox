/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.app.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.core.runtime.RubyRuntimeMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class RubyYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        clearDeployers();
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new RubyYamlParsingProcessor() );
    }

    @Test
    public void testInvalidVersionMetaData() throws Exception {
        try {
            deployResourceAsTorqueboxYml( "ruby-invalid.yml" );
            fail("Should have failed.");
        } catch (Exception e) {
            assertEquals( "2.7 is not a valid value for the enumeration on field version", 
                    e.getCause().getMessage() );
        }
    }

    @Test
    public void testRootless() throws Exception {
        clearDeployers();
        appendDeployer( new AppKnobYamlParsingProcessor() );
        appendDeployer( new RubyYamlParsingProcessor() );
        try {
            deployResourceAs( "rootless-ruby-knob.yml", "rootless-ruby-knob.yml" );
        } catch (DeploymentUnitProcessingException e) {
            assertEquals( "Error processing deployment rootless-ruby-knob.yml: The section ruby " +
                    "requires an app root to be specified, but none has been provided.",
                    e.getMessage() );
        }
    }

    @Test
    public void testWithRuntimeMetaData18() throws Exception {
        MockDeploymentPhaseContext phaseContext = setupResourceAsTorqueboxYml( "ruby-1.8.yml" );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.Version.V1_8, runtimeMetaData.getVersion() );
    }

    @Test
    public void testWithRuntimeMetaData19() throws Exception {
        MockDeploymentPhaseContext phaseContext = setupResourceAsTorqueboxYml( "ruby-1.9.yml" );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.Version.V1_9, runtimeMetaData.getVersion() );
    }

    @Test
    public void testWithRuntimeMetaData20() throws Exception {
        MockDeploymentPhaseContext phaseContext = setupResourceAsTorqueboxYml( "ruby-2.0.yml" );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.Version.V2_0, runtimeMetaData.getVersion() );
    }

    @Test
    public void testWithRuntimeMetaDataCompileModeForce() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-compile-mode-force.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.CompileMode.FORCE, runtimeMetaData.getCompileMode() );
    }

    @Test
    public void testWithRuntimeMetaDataCompileModeJit() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-compile-mode-jit.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.CompileMode.JIT, runtimeMetaData.getCompileMode() );
    }

    @Test
    public void testWithRuntimeMetaDataCompileModeOff() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-compile-mode-off.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertEquals( RubyRuntimeMetaData.CompileMode.OFF, runtimeMetaData.getCompileMode() );
    }

    @Test
    public void testWithRuntimeMetaDataDebugFalse() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-debug-false.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertFalse( runtimeMetaData.isDebug() );
    }

    @Test
    public void testWithRuntimeMetaDataDebugTrue() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-debug-true.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertTrue( runtimeMetaData.isDebug() );
    }

    @Test
    public void testWithRuntimeMetaDataInteractiveFalse() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-interactive-false.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertFalse( runtimeMetaData.isInteractive() );
    }

    @Test
    public void testWithRuntimeMetaDataInteractiveTrue() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-interactive-true.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertTrue( runtimeMetaData.isInteractive() );
    }

    @Test
    public void testWithRuntimeMetaDataProfilingTrue() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-profiling-true.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertTrue( runtimeMetaData.isProfileApi() );
    }

    @Test
    public void testWithRuntimeMetaDataProfilingFalse() throws Exception {
        URL rubyYml = getClass().getResource( "ruby-profiling-false.yml" );
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", rubyYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = new RubyRuntimeMetaData();
        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData2 = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        assertSame( runtimeMetaData, runtimeMetaData2 );
        assertFalse( runtimeMetaData.isProfileApi() );
    }

}
