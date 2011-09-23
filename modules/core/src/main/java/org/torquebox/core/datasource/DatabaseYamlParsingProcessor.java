package org.torquebox.core.datasource;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;

public class DatabaseYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {
    
    public DatabaseYamlParsingProcessor() {
        setSectionName( "database" );
        setSupportsStandalone( true );
    }

    @Override
    protected void parse(DeploymentUnit unit, Object data) throws Exception {
        Map<String, Map<String, Object>> file = (Map<String, Map<String, Object>>) data;
        
        for ( String configurationName : file.keySet() ) {
            Map<String, Object> config = file.get( configurationName );
            DatabaseMetaData md = new DatabaseMetaData( configurationName, config );
            unit.addToAttachmentList( DatabaseMetaData.ATTACHMENTS, md );
        }
    }

}
