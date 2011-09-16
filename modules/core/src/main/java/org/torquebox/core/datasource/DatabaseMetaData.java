package org.torquebox.core.datasource;

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class DatabaseMetaData {
    
    public static final AttachmentKey<AttachmentList<DatabaseMetaData>> ATTACHMENTS = AttachmentKey.createList( DatabaseMetaData.class );
    
    public DatabaseMetaData(String configurationName, Map<String, Object> config) {
        this.configurationName = configurationName;
        this.config = config;
    }
    
    public String getConfigurationName() {
        return this.configurationName;
    }
    
    public Map<String,Object> getConfiguration() {
        return this.config;
    }
    
    public String toString() {
        return "[Database: " + this.configurationName + ": " + this.config + "]";
    }

    private String configurationName;
    private Map<String, Object> config;
}
