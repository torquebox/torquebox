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

package org.torquebox.core.injection.jndi;

import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
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
    public ServiceName getServiceName(ServiceTarget serviceTarget, DeploymentUnit unit) {
        return wrapWithManager( serviceTarget, unit, getServiceNameInternal() );
    }

    protected ServiceName getServiceNameInternal() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( getName() );
    }

    protected boolean serviceIsAlreadyWrapped(DeploymentUnit unit, ServiceName serviceName) {
        return (unit.getServiceRegistry().getService( serviceName ) != null);
    }

    protected ServiceName wrapWithManager(ServiceTarget serviceTarget, DeploymentUnit unit, ServiceName serviceName) {
        ServiceName managementServiceName = unit.getServiceName().append( serviceName ).append( "manager" );

        synchronized(unit.getServiceRegistry()) {
            if (serviceIsAlreadyWrapped( unit, managementServiceName )) {
                return managementServiceName;
            }

            ManagedReferenceInjectableService managementService = new ManagedReferenceInjectableService();
            serviceTarget.addService( managementServiceName, managementService )
            .addDependency( serviceName, ManagedReferenceFactory.class, managementService.getManagedReferenceFactoryInjector() )
            .install();
        }
        return managementServiceName;

    }

}
