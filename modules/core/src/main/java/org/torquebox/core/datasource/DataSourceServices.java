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

package org.torquebox.core.datasource;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;

public class DataSourceServices {

    public static boolean enabled = Boolean.parseBoolean( System.getProperty( "org.torquebox.core.datasource.enabled", "true" ) );
    
    public static ServiceName driverName(DeploymentUnit unit, String driverType) {
        return unit.getServiceName().append( "jdbc", "drivers", driverType );
    }
    
    public static ServiceName datasourceName(DeploymentUnit unit, String dsName) {
        return unit.getServiceName().append( "jdbc", "data-sources", dsName );
    }
    
    public static ServiceName dataSourceInfoName(DeploymentUnit unit) {
        return unit.getServiceName().append(  "xa-ds-info" );
        
    }
    
    public static String jndiName(DeploymentUnit unit, String dsName) {
        return "java:/torquebox/datasources/" + unit.getName() + "/" + dsName;
    }

    public static ServiceName jdbcDriverLoadingRuntimeName(DeploymentUnit unit) {
        return unit.getServiceName().append(  "runtime", "jdbc-loader" );
    }

}
