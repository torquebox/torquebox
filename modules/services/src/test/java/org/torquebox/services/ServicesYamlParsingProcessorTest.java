/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.services;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class ServicesYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUp() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new ServicesYamlParsingProcessor() );
    }

    /** Ensure that an empty services.yml causes no problems. */
    @Test
    public void testEmptyServicesYml() throws Exception {
        URL servicesYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", servicesYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        deploy( phaseContext );
    }

    /** Ensure that a valid services.yml attaches metadata. */
    @Ignore
    @Test
    public void testValidServicesYml() throws Exception {
        
        URL servicesYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", servicesYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        deploy( phaseContext );
        
        List<ServiceMetaData> allMetaData = unit.getAttachmentList( ServiceMetaData.ATTACHMENTS_KEY );
        
        assertEquals( 2, allMetaData.size() );
    }

    @Test
    @Ignore
    public void testRequiresSingletonHandlesNullParams() throws Exception {
        //assertFalse( this.deployer.requiresSingleton( null ) );
    }

    @Test
    @Ignore
    public void testRequiresSingletonReturnsFalseWhenNoSingletonKey() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "key_other_than_singleton", "value" );
        //assertFalse( this.deployer.requiresSingleton( params ) );
    }

    @Test
    @Ignore
    public void testRequiresSingletonReturnsFalseWhenSingletonKeyIsFalse() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "singleton", false );
        //assertFalse( this.deployer.requiresSingleton( params ) );
    }

    @Test
    @Ignore
    public void testRequiresSingletonReturnsTrueWhenSingletonKeyIsTrue() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "singleton", true );
        //assertTrue( this.deployer.requiresSingleton( params ) );
    }

}
