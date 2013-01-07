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

package org.torquebox.core.processors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.net.URL;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class TorqueBoxYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUp() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
    }

    @Test(expected = DeploymentUnitProcessingException.class)
    public void testBrokenAppRackYml() throws Exception {
        URL torqueboxYml = getClass().getResource( "broken-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", torqueboxYml );
        deploy( phaseContext );
    }

    @Test
    public void testEmptyAppRackYml() throws Exception {
        URL torqueboxYml = getClass().getResource( "empty-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", torqueboxYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
        
        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
        assertNotNull( metaData );
    }

    @Test
    public void testSectionsTorqueBoxYaml() throws Exception {
        URL torqueboxYml = getClass().getResource( "full-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", torqueboxYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
        

        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
        assertNotNull( metaData );

        assertNotNull( metaData.getSection( "application" ) );
        assertNotNull( metaData.getSection( "app" ) );
        assertSame( metaData.getSection("app"), metaData.getSection( "application" ) );
        assertNotNull( metaData.getSection( "web" ) );
        assertNotNull( metaData.getSection( "queues" ) );
        assertNotNull( metaData.getSection( "topics" ) );
        assertNotNull( metaData.getSection( "messaging" ) );
        assertNotNull( metaData.getSection( "services" ) );
        assertNotNull( metaData.getSection( "jobs" ) );
        assertNotNull( metaData.getSection( "ruby" ) );
    }



}
