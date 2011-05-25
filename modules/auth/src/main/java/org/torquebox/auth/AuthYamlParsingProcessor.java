package org.torquebox.auth;

import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;


public class AuthYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public AuthYamlParsingProcessor() {
        setSectionName( "auth" );
        setSupportsStandalone( true );
    }

	@Override
	protected void parse(DeploymentUnit unit, Object dataObject) throws Exception {
        log.info( "parsing: " + dataObject );

		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) dataObject;

        if (data != null) {
        	for( String name: data.keySet() ) {
        		@SuppressWarnings("unchecked")
				Map<String, Object> config = (Map<String, Object>) data.get(name);
        		log.info("Loading auth configuration for " + name + ":" + config.get("domain"));
        		AuthMetaData metaData = new AuthMetaData();
        		metaData.addAuthentication( name, config );
                unit.addToAttachmentList( AuthMetaData.ATTACHMENT_KEY, metaData );
        	}
        }
        else {
        	log.info("No jaas auth configured. Moving on.");
        }
	}
	
    private static final Logger log = Logger.getLogger( "org.torquebox.auth" );
}
