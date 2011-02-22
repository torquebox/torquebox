package org.torquebox.base.deployers;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class AuthYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {
	
    public void AppKnobYamlParsingDeployer() {
        setSectionName( "auth" );
        setSupportsStandalone( true);
        addInput( RubyApplicationMetaData.class );
        addOutput( RubyApplicationMetaData.class );
        setStage( DeploymentStages.PARSE );
    }

	@Override
	@SuppressWarnings("unchecked")
	public void parse(VFSDeploymentUnit unit, Object dataObject) throws Exception {

		RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) dataObject;
        
        Map<String, Map<String, String>> authConfig = appMetaData.getAuthenticationConfig();
        if (authConfig == null) {
        	authConfig = new HashMap<String, Map<String, String>>();
        	appMetaData.setAuthenticationConfig(authConfig);
        }
        authConfig.putAll(data);
	}
}
