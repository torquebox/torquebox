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

package org.torquebox.core.injection.jndi;

import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;

/**
 * Injectable for JNDI-discovered items.
 * 
 * @author Bob McWhirter
 */
public class JNDIInjectable extends SimpleNamedInjectable {

    public JNDIInjectable(String name, boolean generic) {
        this( "jndi", name, generic );
    }

    protected JNDIInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext context) {
        return wrapWithManager( context, getServiceNameInternal() );
    }

    protected ServiceName getServiceNameInternal() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( getName() );
    }

    protected boolean serviceIsAlreadyWrapped(DeploymentPhaseContext context, ServiceName serviceName) {
        return (context.getServiceRegistry().getService( serviceName ) != null);
    }

    protected ServiceName wrapWithManager(DeploymentPhaseContext context, ServiceName serviceName) {
        ServiceName managementServiceName = context.getDeploymentUnit().getServiceName().append( serviceName ).append( "manager" );

        if (serviceIsAlreadyWrapped( context, managementServiceName )) {
            return managementServiceName;
        }

        ManagedReferenceInjectableService managementService = new ManagedReferenceInjectableService();
        context.getServiceTarget().addService( managementServiceName, managementService )
                .addDependency( serviceName, ManagedReferenceFactory.class, managementService.getManagedReferenceFactoryInjector() )
                .install();

        return managementServiceName;

    }

}
