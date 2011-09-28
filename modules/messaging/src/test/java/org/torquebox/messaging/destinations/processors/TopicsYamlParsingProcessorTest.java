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

package org.torquebox.messaging.destinations.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.projectodd.polyglot.test.as.AbstractDeploymentProcessorTestCase;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.messaging.destinations.TopicMetaData;
import org.torquebox.messaging.destinations.processors.TopicsYamlParsingProcessor;

public class TopicsYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor()  );
        appendDeployer( new TopicsYamlParsingProcessor()  );
    }

    @Test
    public void testEmptyYaml() throws Exception {
        URL topicsYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "topics.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testJunkYaml() throws Exception {
        URL topicsYml = getClass().getResource( "junk-topics.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "topics.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testValidYaml() throws Exception {
        URL topicsYml = getClass().getResource( "valid-topics.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "topics.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );

        assertEquals( 2, allMetaData.size() );

        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/foo" );
        assertNotNull( topicFoo );

        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/bar" );
        assertNotNull( topicBar );

    }

    @Test
    public void testTorqueBoxYml() throws Exception {
        URL topicsYml = getClass().getResource( "valid-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/tbyaml/foo" );
        assertNotNull( topicFoo );

        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/tbyaml/bar" );
        assertNotNull( topicBar );
    }

    @Ignore @Test
    public void testTorqueBoxYmlWins() throws Exception {
//        JavaArchive jar = createJar( "mystuff.jar" );
//        jar.addResource( getClass().getResource( "/valid-topics.yml" ), "/META-INF/topics.yml" );
//        jar.addResource( getClass().getResource( "/valid-torquebox.yml" ), "/META-INF/torquebox.yml" );
//        String deploymentName = addDeployment( createJarFile( jar ) );
//
//        processDeployments( true );
//
//        DeploymentUnit unit = getDeploymentUnit( deploymentName );
//        Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );
//
//        assertFalse( allMetaData.isEmpty() );
//
//        assertEquals( 2, allMetaData.size() );
//
//        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/tbyaml/foo" );
//        assertNotNull( topicFoo );
//
//        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/tbyaml/bar" );
//        assertNotNull( topicBar );
//
//        undeploy( deploymentName );
    }

    private TopicMetaData getMetaData(List<TopicMetaData> allMetaData, String name) {
        for (TopicMetaData each : allMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }

        return null;
    }

}
