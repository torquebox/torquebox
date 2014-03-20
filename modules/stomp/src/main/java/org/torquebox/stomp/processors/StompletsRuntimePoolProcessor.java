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

package org.torquebox.stomp.processors;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.stomp.RubyStompletMetaData;

 /* 
 * Ensures that pool metadata for stomplets is available
 */
public class StompletsRuntimePoolProcessor implements DeploymentUnitProcessor {

    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }

        List<RubyStompletMetaData> allMetaData = unit.getAttachmentList( RubyStompletMetaData.ATTACHMENTS_KEY ); 
        if (allMetaData.isEmpty()) {
            return;
        }
        
        List<PoolMetaData> allPoolMetaData = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        PoolMetaData poolMetaData = PoolMetaData.extractNamedMetaData( allPoolMetaData, "stomplets" );
        
        if ( poolMetaData == null ) {
            poolMetaData = new PoolMetaData("stomplets");
            poolMetaData.setMinimumSize( allMetaData.size()  );
            poolMetaData.setMaximumSize( allMetaData.size()  );
            unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, poolMetaData );
        }
    }
    
    public void undeploy(DeploymentUnit unit) {
        
    }

}
