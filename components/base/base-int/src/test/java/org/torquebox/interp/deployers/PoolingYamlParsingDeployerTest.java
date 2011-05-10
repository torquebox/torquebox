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

package org.torquebox.interp.deployers;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.pool.PoolMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class PoolingYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private PoolingYamlParsingDeployer deployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new PoolingYamlParsingDeployer();
        addDeployer( this.deployer );
    }

    @Test
    public void testEmptyPoolingYml() throws Exception {

        URL poolingYml = getClass().getResource( "empty-pooling.yml" );

        String deploymentName = addDeployment( poolingYml, "pooling.yml" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        assertTrue( unit.getAllMetaData( PoolMetaData.class ).isEmpty() );

    }

    @Test
    public void testJunkPoolingYml() throws Exception {

        URL poolingYml = getClass().getResource( "junk-pooling.yml" );

        String deploymentName = addDeployment( poolingYml, "pooling.yml" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        assertTrue( unit.getAllMetaData( PoolMetaData.class ).isEmpty() );

    }

    @Test
    public void testMinMaxPoolingYml() throws Exception {
        URL poolingYml = getClass().getResource( "min-max-pooling.yml" );

        String deploymentName = addDeployment( poolingYml, "pooling.yml" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends PoolMetaData> pools = unit.getAllMetaData( PoolMetaData.class );

        assertFalse( pools.isEmpty() );
        assertEquals( 2, pools.size() );

        PoolMetaData poolOne = findPool( "pool_one", pools );
        assertNotNull( poolOne );

        assertEquals( 1, poolOne.getMinimumSize() );
        assertEquals( 1, poolOne.getMaximumSize() );

        PoolMetaData poolTwo = findPool( "pool_two", pools );
        assertNotNull( poolTwo );

        assertEquals( 10, poolTwo.getMinimumSize() );
        assertEquals( 200, poolTwo.getMaximumSize() );
    }

    protected PoolMetaData findPool(String name, Set<? extends PoolMetaData> pools) {

        for (PoolMetaData each : pools) {
            if (each.getName().equals( name )) {
                return each;
            }
        }
        return null;
    }

}
