package org.torquebox.core.datasource;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;

public class DataSourceServices {
    
    
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

}
