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

package org.torquebox.cdi.injection;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.weld.WeldStartService;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.torquebox.core.injection.SimpleNamedInjectable;

public class CDIInjectable extends SimpleNamedInjectable {

    public CDIInjectable(String name, boolean generic) {
        super( "cdi", name, generic );
    }
    
    @Override
    public String getKey() {
        String fullName = getName();

        int lastDot = fullName.lastIndexOf( '.' );

        String className = null;
        String packageName = null;

        if (lastDot > 0) {
            className = fullName.substring( lastDot + 1 );
            packageName = fullName.substring( 0, lastDot );
        } else {
            className = fullName;
        }
        return "Java::" + getPackageKeyPart( packageName ) + "::" + className;

    }

    public String getPackageKeyPart(String packageName) {
        if ( packageName == null ) {
            return "Default";
        }
        
        int length = packageName.length();

        StringBuilder buf = new StringBuilder();
        
        for (int start = 0, offset = 0; start < length; start = offset + 1) {
            if ((offset = packageName.indexOf( '.', start )) == -1) {
                offset = length;
            }
            buf.append( Character.toUpperCase( packageName.charAt( start ) ) ).append( packageName.substring( start + 1, offset ) );
        }
        return buf.toString();
    }

    @Override
    public ServiceName getServiceName(ServiceTarget serviceTarget, DeploymentUnit unit) throws ClassNotFoundException {
        ServiceName injectionServiceName = unit.getServiceName().append( "cdi-injection" ).append( getName() );

        synchronized(unit.getServiceRegistry()) {
            if (unit.getServiceRegistry().getService( injectionServiceName ) != null) {
                return injectionServiceName;
            }

            Module module = unit.getAttachment( Attachments.MODULE );
            Class<?> injectionType = module.getClassLoader().loadClass( getName() );

            ServiceName weldServiceName = unit.getServiceName().append( WeldStartService.SERVICE_NAME );
            CDIInjectableService injectionService = new CDIInjectableService( injectionType );
            serviceTarget.addService( injectionServiceName, injectionService )
            .addDependency( weldServiceName, WeldStartService.class, injectionService.getWeldStartServiceInjector() )
            .install();
        }
        return injectionServiceName;
    }

}
