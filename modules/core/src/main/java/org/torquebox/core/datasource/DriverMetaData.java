package org.torquebox.core.datasource;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class DriverMetaData {
    
    public static final AttachmentKey<AttachmentList<DriverMetaData>> ATTACHMENTS = AttachmentKey.createList( DriverMetaData.class );
    
    public DriverMetaData(String adapterName, String driverClassName) {
        this.adapterName = adapterName;
        this.driverClassName = driverClassName;
    }
    
    public String getAdapterName() {
        return this.adapterName;
    }
    
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
    
    public String getDriverClassName() {
        return this.driverClassName;
    }
    
    public void setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
    }
    
    public String getDataSourceClassName() {
        return this.dataSourceClassName;
    }
    
    public String toString() {
        return "[Driver: " + this.adapterName + "]";
    }

    private String adapterName;
    private String driverClassName;
    private String dataSourceClassName;
}
