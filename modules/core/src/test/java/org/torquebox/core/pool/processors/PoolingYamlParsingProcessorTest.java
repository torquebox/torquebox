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

package org.torquebox.core.pool.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.polyglot.test.as.AbstractDeploymentProcessorTestCase;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.torquebox.core.pool.processors.PoolingYamlParsingProcessor;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.core.runtime.PoolMetaData;

public class PoolingYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new PoolingYamlParsingProcessor() );
    }

    @Test
    public void testEmptyPoolingYml() throws Exception {

        URL poolingYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", poolingYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );

        assertTrue( unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY ).isEmpty() );

    }

    @Test(expected=DeploymentUnitProcessingException.class)
    public void testJunkPoolingYml() throws Exception {

        URL poolingYml = getClass().getResource( "junk-pooling.yml" );
        
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", poolingYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
    }

    @Test
    public void testMinMaxPoolingYml() throws Exception {
        URL poolingYml = getClass().getResource( "min-max-pooling.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", poolingYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
        
        List<PoolMetaData> pools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );

        assertFalse( pools.isEmpty() );
        assertEquals( 2, pools.size() );
        
        PoolMetaData poolOne = pools.get( 0 );
        assertNotNull( poolOne );

        assertEquals( "pool_one", poolOne.getName() );
        assertEquals( 1, poolOne.getMinimumSize() );
        assertEquals( 1, poolOne.getMaximumSize() );

        PoolMetaData poolTwo = pools.get( 1 );

        assertEquals( "pool_two", poolTwo.getName() );
        assertEquals( 10, poolTwo.getMinimumSize() );
        assertEquals( 200, poolTwo.getMaximumSize() );
    }

}
