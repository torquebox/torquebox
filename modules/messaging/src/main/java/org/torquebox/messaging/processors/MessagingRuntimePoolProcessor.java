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

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.messaging.MessageProcessorMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: EnvironmentMetaData, PoolMetaData, MessageProcessorMetaData
 *   Out: PoolMetaData
 * </pre>
 * 
 * Ensures that pool metadata for messaging is available
 */
public class MessagingRuntimePoolProcessor implements DeploymentUnitProcessor {

    private String instanceFactoryName;

    /**
     * I'd rather use setInput(MessageProcessorMetaData) and omit the
     * getAllMetaData short circuit in deploy(), but that requires attachers to
     * pass an ExpectedType, and I don't think we can assume that.
     */
    public MessagingRuntimePoolProcessor() {
    }


    @Override
    public void deploy(DeploymentPhaseContext context) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = context.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        
        if ( ! unit.hasAttachment( MessageProcessorMetaData.ATTACHMENTS_KEY ) ) {
            return;
        }

        List<PoolMetaData> pools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );

        for (PoolMetaData pool : pools) {
            if (pool.getName().equals( "messaging" )) {
                return;
            }
        }
        RubyAppMetaData envMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        boolean devMode = envMetaData != null && envMetaData.isDevelopmentMode();
        PoolMetaData pool = devMode ? new PoolMetaData( "messaging", 1, 10 ) : new PoolMetaData( "messaging" );
        unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, pool );
    }


    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
