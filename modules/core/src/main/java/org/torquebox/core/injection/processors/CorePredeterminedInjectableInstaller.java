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

package org.torquebox.core.injection.processors;


import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.injection.CorePredeterminedInjectableHandler;
import org.torquebox.core.injection.ServiceRegistryInjectable;
import org.torquebox.core.injection.ServiceTargetInjectable;

/**
 * Processor which publishes the <code>ServiceRegistry</code> and
 * <code>ServiceTarget</code> for each deployment for later used by injections.
 * 
 * @see ServiceRegistryInjectable
 * @see ServiceTargetInjectable
 * @see CorePredeterminedInjectableHandler
 * 
 * @author Bob McWhirter
 */
public class CorePredeterminedInjectableInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }

        if (!unit.hasAttachment( RubyAppMetaData.ATTACHMENT_KEY )) {
            return;
        }

        ServiceTarget serviceTarget = phaseContext.getServiceTarget();
        ServiceRegistry serviceRegistry = unit.getServiceRegistry();

        serviceTarget.addService( CoreServices.serviceRegistryName( unit ), new ValueService<ServiceRegistry>( new ImmediateValue<ServiceRegistry>( serviceRegistry ) ) )
                .install();
        serviceTarget.addService( CoreServices.serviceTargetName( unit ), new ValueService<ServiceTarget>( new ImmediateValue<ServiceTarget>( serviceTarget ) ) )
                .install();

    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }  
    
}
