package org.torquebox.core.datasource;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;

public class DataSourceMetaData {

    public DataSourceMetaData(String name, String driverName, String dataSourceClassName) {
        this.name = name;
        this.driverName = driverName;
        this.dataSourceClassName = dataSourceClassName;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDriverName() {
        return this.driverName;
    }
    
    public String getDataSourceClassName() {
        return this.dataSourceClassName;
    }
    
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }
    
    public String getJndiName() {
        return this.jndiName;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public Map<String, String> getProperties() {
        return this.properties;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    
    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }
    
    public void setSecurity(DsSecurity security) {
        this.security = security;
    }
    
    public DsSecurity getSecurity() {
        return this.security;
    }

    public static AttachmentKey<AttachmentList<DataSourceMetaData>> ATTACHMENTS = AttachmentKey.createList( DataSourceMetaData.class );
    
    private String name;
    private String driverName;
    private String dataSourceClassName;
    
    private String jndiName;
    
    private DsSecurity security;
    
    private int maxPoolSize = 5;

    private Map<String, String> properties = new HashMap<String, String>();

}
