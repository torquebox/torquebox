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

package org.torquebox.core.runtime.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.runtime.RubyRuntimeMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class BaseRubyRuntimeInstallerTest extends AbstractDeploymentProcessorTestCase {
    
    private Map<String, String> environment = new HashMap<String, String>();
    private File root;
    
    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new BaseRubyRuntimeInstaller() );
        this.root = new File( "/foo" );
    }

    @Test
    public void testHappy() throws Exception {
        environment.put( "SOME_VAR", "gassy" );
        RubyAppMetaData rubyAppMetaData = new RubyAppMetaData( "app_name");

        rubyAppMetaData.setRoot( this.root );
        rubyAppMetaData.setEnvironmentVariables( environment );

        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        rubyAppMetaData.attachTo( unit );
                
        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        assertNotNull( runtimeMetaData );
        assertEquals( this.root, runtimeMetaData.getBaseDir() );
        assertTrue( runtimeMetaData.getEnvironment().containsKey( "SOME_VAR" ) );
        assertEquals( RubyRuntimeMetaData.RuntimeType.BARE, runtimeMetaData.getRuntimeType() );
    }


    @Test
    public void testWithExistingRubyRuntimeMD() throws Exception {
        RubyAppMetaData rubyAppMetaData = new RubyAppMetaData( "app_name");

        rubyAppMetaData.setRoot( this.root );

        RubyRuntimeMetaData existingRuntimeMD = new RubyRuntimeMetaData();
        
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, existingRuntimeMD );
        rubyAppMetaData.attachTo( unit );

        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        assertNotNull( runtimeMetaData );
        assertEquals( existingRuntimeMD, runtimeMetaData );
        assertEquals( this.root, runtimeMetaData.getBaseDir() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.BARE, runtimeMetaData.getRuntimeType() );
    }


    @Test
    public void testWithExistingTypedRubyRuntimeMD() throws Exception {
        RubyAppMetaData rubyAppMetaData = new RubyAppMetaData( "app_name");

        rubyAppMetaData.setRoot( this.root );

        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        RubyRuntimeMetaData existingRuntimeMD = new RubyRuntimeMetaData();
        existingRuntimeMD.setRuntimeType( RubyRuntimeMetaData.RuntimeType.RACK );

        unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, existingRuntimeMD );
        rubyAppMetaData.attachTo( unit );
        
        deploy( phaseContext );

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        assertNotNull( runtimeMetaData );
        assertEquals( existingRuntimeMD, runtimeMetaData );
        assertNull( runtimeMetaData.getBaseDir() );
        assertEquals( RubyRuntimeMetaData.RuntimeType.RACK, runtimeMetaData.getRuntimeType() );
    }

}
