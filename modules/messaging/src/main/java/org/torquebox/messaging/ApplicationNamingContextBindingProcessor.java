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

package org.torquebox.messaging;

import org.jboss.as.ee.naming.RootContextService;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.app.RubyApplicationMetaData;

public class ApplicationNamingContextBindingProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        if (appMetaData == null) {
            return;
        }

        RootContextService contextService = new RootContextService();
        ServiceName contextServiceName = ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( "queue" ).append( appMetaData.getApplicationName() );
        phaseContext.getServiceTarget().addService( contextServiceName, contextService )
                .install();
        
        RootContextService tasksService = new RootContextService();
        ServiceName tasksServiceName = contextServiceName.append(  "tasks"  );
        
        phaseContext.getServiceTarget().addService( tasksServiceName, tasksService )
            .addDependency(  contextServiceName )
            .install();

    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

}
