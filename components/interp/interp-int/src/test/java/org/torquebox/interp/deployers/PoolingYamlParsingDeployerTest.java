package org.torquebox.interp.deployers;

import java.net.URL;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

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
