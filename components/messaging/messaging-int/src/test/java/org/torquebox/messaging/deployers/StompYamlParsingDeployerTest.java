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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.deployers.TorqueBoxYamlParsingDeployer;
import org.torquebox.messaging.metadata.StompMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class StompYamlParsingDeployerTest extends AbstractDeployerTestCase {

	private TorqueBoxYamlParsingDeployer globalDeployer;
	private StompYamlParsingDeployer stompDeployer;

	@Before
	public void setUpDeployer() throws Throwable {
		this.stompDeployer = new StompYamlParsingDeployer();
		addDeployer( this.stompDeployer );

		this.globalDeployer = new TorqueBoxYamlParsingDeployer();
		addDeployer( this.globalDeployer );
	}

	@Test
	public void testEmptyYaml() throws Exception {
		File config = new File( System.getProperty( "user.dir" ), "src/test/resources/empty-stomp.yml" );
		String deploymentName = addDeployment( config.toURI().toURL(), "stomp.yml" );

		processDeployments( true );

		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends StompMetaData> allMetaData = unit.getAllMetaData( StompMetaData.class );

		assertTrue( allMetaData.isEmpty() );

		undeploy( deploymentName );
	}

	@Test
	public void testJunkYaml() throws Exception {
		File config = new File( System.getProperty( "user.dir" ), "src/test/resources/junk-stomp.yml" );
		String deploymentName = addDeployment( config.toURI().toURL(), "stomp.yml" );

		processDeployments( true );

		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends StompMetaData> allMetaData = unit.getAllMetaData( StompMetaData.class );

		assertTrue( allMetaData.isEmpty() );

		undeploy( deploymentName );
	}

	@Test
	public void testValidYaml() throws Exception {
		File config = new File( System.getProperty( "user.dir" ), "src/test/resources/valid-stomp.yml" );
		String deploymentName = addDeployment( config.toURI().toURL(), "stomp.yml" );

		processDeployments( true );

		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends StompMetaData> allMetaData = unit.getAllMetaData( StompMetaData.class );

		assertFalse( allMetaData.isEmpty() );
		assertEquals( 1, allMetaData.size() );

		StompMetaData data = allMetaData.iterator().next();
		assertEquals( data.getPort(), 9001 );

		undeploy( deploymentName );
	}

	@Test
	public void testTorqueBoxYml() throws Exception {
		String deploymentName = addDeployment( getClass().getResource( "/valid-torquebox.yml" ), "torquebox.yml" );

		processDeployments( true );

		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends StompMetaData> allMetaData = unit.getAllMetaData( StompMetaData.class );

		assertFalse( allMetaData.isEmpty() );
		assertEquals( 1, allMetaData.size() );

		StompMetaData data = allMetaData.iterator().next();
		assertEquals( data.getPort(), StompMetaData.DEFAULT_PORT );

		undeploy( deploymentName );
	}

	@Test
	public void testTorqueBoxYmlWins() throws Exception {
		JavaArchive jar = createJar( "mystuff.jar" );
		jar.addResource( getClass().getResource( "/valid-stomp.yml" ), "/META-INF/stomp.yml" );
		jar.addResource( getClass().getResource( "/valid-torquebox.yml" ), "/META-INF/torquebox.yml" );
		String deploymentName = addDeployment( createJarFile( jar ) );

		processDeployments( true );

		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends StompMetaData> allMetaData = unit.getAllMetaData( StompMetaData.class );

		assertFalse( allMetaData.isEmpty() );
		assertEquals( 1, allMetaData.size() );

		StompMetaData data = allMetaData.iterator().next();
		assertEquals( data.getPort(), StompMetaData.DEFAULT_PORT );

		undeploy( deploymentName );
	}

}
