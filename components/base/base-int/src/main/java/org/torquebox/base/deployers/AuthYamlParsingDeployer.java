package org.torquebox.base.deployers;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.metadata.AuthMetaData;

public class AuthYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {
	
    public static final String DEFAULT_STRATEGY = "file";
	public static final String DEFAULT_DOMAIN   = "other";
	
    public AuthYamlParsingDeployer() {
        setSectionName( "auth" );
        setSupportsStandalone( true);
        addInput( AuthMetaData.class );
        addOutput( AuthMetaData.class );
        setStage( DeploymentStages.PARSE );
    }

	@Override
	@SuppressWarnings("unchecked")
	public void parse(VFSDeploymentUnit unit, Object dataObject) throws Exception {

		AuthMetaData authMetaData = unit.getAttachment( AuthMetaData.class );
		if (authMetaData == null) {
			log.debug("Configuring TorqueBox Authentication");
			authMetaData = new AuthMetaData();
			unit.addAttachment(AuthMetaData.class, authMetaData);
		}
		
        Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) dataObject;
        if (data == null || data.size() < 1) {
    		log.warn("No auth configuration provided. Using defaults");
        	Map<String, String> defaultConfig = new HashMap<String, String>();
        	defaultConfig.put("domain", AuthYamlParsingDeployer.DEFAULT_DOMAIN);
        	defaultConfig.put("strategy", AuthYamlParsingDeployer.DEFAULT_STRATEGY);
        	data.put("default", defaultConfig);
        }
        for(String name: data.keySet()) {
    		String domain   = data.get(name).get("domain");    		
    		String strategy = data.get(name).get("strategy");
        	authMetaData.addAuthentication(name, domain, strategy);
        }
	}
}
