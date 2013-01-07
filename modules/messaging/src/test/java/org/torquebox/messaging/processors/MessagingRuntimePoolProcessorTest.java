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

package org.torquebox.messaging.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class MessagingRuntimePoolProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new MessagingRuntimePoolProcessor() );
    }

    /**
     * Ensure a deployment without a defined pool and with message processors
     * does define a pool.
     */
    @Test
    public void testPoolRequired() throws Exception {
        DeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        unit.addToAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY, new MessageProcessorMetaData() );

        deploy( phaseContext );

        List<PoolMetaData> allPools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        assertEquals( 1, allPools.size() );

        PoolMetaData messagingPoolMetaData = allPools.get( 0 );
        assertEquals( "messaging", messagingPoolMetaData.getName() );
        assertTrue( messagingPoolMetaData.isShared() );
        assertTrue( messagingPoolMetaData.isDeferUntilRequested() );
    }

}
