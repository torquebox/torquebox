package org.torquebox.base.deployers;

import java.util.Map;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.metadata.AuthMetaData;

public class AuthYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {
	
    public AuthYamlParsingDeployer() {
        setSectionName( "auth" );
        setSupportsStandalone( true );
        addInput( AuthMetaData.class );
        addOutput( AuthMetaData.class );
        setStage( DeploymentStages.PARSE );
    }

	@Override
	@SuppressWarnings("unchecked")
	public void parse(VFSDeploymentUnit unit, Object dataObject) throws Exception {

		AuthMetaData authMetaData = unit.getAttachment( AuthMetaData.class );
		if (authMetaData == null) {
			log.info("Initializing TorqueBox Authentication");
			authMetaData = new AuthMetaData();
			unit.addAttachment(AuthMetaData.class, authMetaData);
		}
		
        Map<String, Object> data = (Map<String, Object>) dataObject;
        if (data != null) {
        	for(String name: data.keySet()) {        	    
        		String domain   = ((Map<String, String>) data.get(name)).get("domain");    		
        		log.info("Configuring TorqueBox authentication for domain ["+domain+"]");
        		authMetaData.addAuthentication(name, domain);
        	}
        }
	}
}
