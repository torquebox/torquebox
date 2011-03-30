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

package org.torquebox.rack.deployers;

import static org.junit.Assert.*;

import java.net.URL;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class WebYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private WebYamlParsingDeployer deployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new WebYamlParsingDeployer();
        addDeployer( this.deployer );
    }

    @Test()
    public void testEmptyWebYml() throws Exception {
        URL appRackYml = getClass().getResource( "empty-web.yml" );

        addDeployment( appRackYml, "web.yml" );
        processDeployments( true );
    }

    @Test
    public void testValidWebYml() throws Exception {
        URL appRackYml = getClass().getResource( "valid-web.yml" );

        String deploymentName = addDeployment( appRackYml, "web.yml" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.class );

        assertNotNull( rackMetaData );

        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get( 0 ) );
        assertNull( rackMetaData.getStaticPathPrefix() );
    }

    @Test
    public void testValidWebYmlCustomStaticPathPrefix() throws Exception {
        URL appRackYml = getClass().getResource( "static-path-web.yml" );

        String deploymentName = addDeployment( appRackYml, "web.yml" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.class );

        assertNotNull( rackMetaData );

        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get( 0 ) );
        assertEquals( "/elsewhere", rackMetaData.getStaticPathPrefix() );
    }
}
