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

package org.torquebox.messaging.destinations.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.projectodd.polyglot.messaging.destinations.QueueMetaData;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.app.processors.AppKnobYamlParsingProcessor;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class QueuesYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        clearDeployers();
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new QueuesYamlParsingProcessor() );
    }

    @Test
    public void testEmptyYaml() throws Exception {
        MockDeploymentUnit unit = deployResourceAs( "empty.yml", "queues.yml" );

        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test(expected = DeploymentUnitProcessingException.class)
    public void testJunkYaml() throws Exception {
        MockDeploymentUnit unit = deployResourceAs( "junk-queues.yml", "queues.yml" );

        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testValidYaml() throws Exception {
        MockDeploymentUnit unit = deployResourceAs( "valid-queues.yml", "queues.yml" );

        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );

        assertEquals( 2, allMetaData.size() );

        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/foo" );
        assertNotNull( queueFoo );

        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/bar" );
        assertNotNull( queueBar );
    }

    @Test
    public void testRootless() throws Exception {

        clearDeployers();
        appendDeployer( new AppKnobYamlParsingProcessor() );
        appendDeployer( new QueuesYamlParsingProcessor() );

        MockDeploymentUnit unit = deployResourceAs( "rootless-queues-knob.yml", "rootless-queues-knob.yml" );
        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );

        assertEquals( 3, allMetaData.size() );

        QueueMetaData queueItchy = getMetaData( allMetaData, "/queues/itchy" );
        assertNotNull( queueItchy );
        assertTrue( queueItchy.isDurable() );

        QueueMetaData queueScratchy = getMetaData( allMetaData, "/queues/scratchy" );
        assertNotNull( queueScratchy );
        assertFalse( queueScratchy.isDurable() );
        
        QueueMetaData queuePoochie = getMetaData( allMetaData, "/queues/poochie" );
        assertNotNull( queuePoochie );
        assertTrue( queuePoochie.isDurable() );
        
    }

    @Test
    public void testTorqueBoxYml() throws Exception {
        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "valid-torquebox.yml" );

        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );

        assertEquals( 3, allMetaData.size() );

        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/tbyaml/foo" );
        assertNotNull( queueFoo );

        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/tbyaml/bar" );
        assertNotNull( queueBar );

        QueueMetaData queueFooBar = getMetaData( allMetaData, "/queues/tbyaml/foobar" );
        assertNotNull( queueFooBar );
    }

    @Ignore
    @Test
    public void testTorqueBoxYmlWins() throws Exception {
        // TODO fix multi-asset deployer tests.

        // JavaArchive jar = createJar( "mystuff.jar" );
        // jar.addResource( getClass().getResource( "/valid-queues.yml" ),
        // "/META-INF/queues.yml" );
        // jar.addResource( getClass().getResource( "/valid-torquebox.yml" ),
        // "/META-INF/torquebox.yml" );
        // String deploymentName = addDeployment( createJarFile( jar ) );
        //
        // processDeployments( true );
        //
        // DeploymentUnit unit = getDeploymentUnit( deploymentName );
        // Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData(
        // QueueMetaData.class );
        //
        // assertFalse( allMetaData.isEmpty() );
        //
        // assertEquals( 3, allMetaData.size() );
        //
        // QueueMetaData queueFoo = getMetaData( allMetaData,
        // "/queues/tbyaml/foo" );
        // assertNotNull( queueFoo );
        //
        // QueueMetaData queueBar = getMetaData( allMetaData,
        // "/queues/tbyaml/bar" );
        // assertNotNull( queueBar );
        //
        // undeploy( deploymentName );
    }

    @Test
    public void testDestinationDurability() throws Exception {

        MockDeploymentUnit unit = deployResourceAsTorqueboxYml( "valid-torquebox.yml" );

        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );

        assertEquals( 3, allMetaData.size() );

        // /queues/tbyaml/foo has no durability flag set, we should default to
        // durable
        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/tbyaml/foo" );
        assertNotNull( queueFoo );
        assertTrue( queueFoo.isDurable() );

        // /queues/tbyaml/bar has durability set to true, we should reflect that
        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/tbyaml/bar" );
        assertNotNull( queueBar );
        assertFalse( queueBar.isDurable() );

        // /queues/tbyaml/bar has durability set to false, we should reflect
        // that
        QueueMetaData queueFooBar = getMetaData( allMetaData, "/queues/tbyaml/foobar" );
        assertNotNull( queueFooBar );
        assertTrue( queueFooBar.isDurable() );

    }

    private QueueMetaData getMetaData(List<QueueMetaData> allMetaData, String name) {
        for (QueueMetaData each : allMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }

        return null;
    }

}
