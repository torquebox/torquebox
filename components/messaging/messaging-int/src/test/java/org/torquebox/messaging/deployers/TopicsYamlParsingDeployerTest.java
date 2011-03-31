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

package org.torquebox.messaging.deployers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.deployers.TorqueBoxYamlParsingDeployer;
import org.torquebox.messaging.metadata.TopicMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class TopicsYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private TorqueBoxYamlParsingDeployer globalDeployer;
    private TopicsYamlParsingDeployer topicsDeployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.topicsDeployer = new TopicsYamlParsingDeployer();
        addDeployer( this.topicsDeployer );

        this.globalDeployer = new TorqueBoxYamlParsingDeployer();
        addDeployer( this.globalDeployer );
    }

    @Test
    public void testEmptyYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/empty-topics.yml" );
        String deploymentName = addDeployment( config.toURI().toURL(), "topics.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );

        assertTrue( allMetaData.isEmpty() );

        undeploy( deploymentName );
    }

    @Test
    public void testJunkYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/junk-topics.yml" );
        String deploymentName = addDeployment( config.toURI().toURL(), "topics.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );

        assertTrue( allMetaData.isEmpty() );

        undeploy( deploymentName );
    }

    @Test
    public void testValidYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/valid-topics.yml" );
        String deploymentName = addDeployment( config.toURI().toURL(), "topics.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/foo" );
        assertNotNull( topicFoo );

        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/bar" );
        assertNotNull( topicBar );

        undeploy( deploymentName );
    }

    @Test
    public void testTorqueBoxYml() throws Exception {
        String deploymentName = addDeployment( getClass().getResource( "/valid-torquebox.yml" ), "torquebox.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/tbyaml/foo" );
        assertNotNull( topicFoo );

        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/tbyaml/bar" );
        assertNotNull( topicBar );

        undeploy( deploymentName );
    }

    @Test
    public void testTorqueBoxYmlWins() throws Exception {
        JavaArchive jar = createJar( "mystuff.jar" );
        jar.addResource( getClass().getResource( "/valid-topics.yml" ), "/META-INF/topics.yml" );
        jar.addResource( getClass().getResource( "/valid-torquebox.yml" ), "/META-INF/torquebox.yml" );
        String deploymentName = addDeployment( createJarFile( jar ) );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/tbyaml/foo" );
        assertNotNull( topicFoo );

        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/tbyaml/bar" );
        assertNotNull( topicBar );

        undeploy( deploymentName );
    }

    private TopicMetaData getMetaData(Set<? extends TopicMetaData> allMetaData, String name) {
        for (TopicMetaData each : allMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }

        return null;
    }

}
