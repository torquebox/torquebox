package org.torquebox.auth;

import java.util.Collection;

import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.auth.AuthMetaData.Config;
import org.torquebox.core.app.RubyApplicationMetaData;

public class AuthDeployer implements DeploymentUnitProcessor {

	@Override
	public void deploy(DeploymentPhaseContext phaseContext)
			throws DeploymentUnitProcessingException {
		DeploymentUnit unit = phaseContext.getDeploymentUnit();
		
		// We need the application name to name our bean with
        RubyApplicationMetaData appMetaData = unit.getAttachment(RubyApplicationMetaData.ATTACHMENT_KEY);
        String applicationName = appMetaData.getApplicationName();
        this.setApplicationName(applicationName);

        // Install authenticators for every domain
        AttachmentList<AuthMetaData> allMetaData = unit.getAttachment(AuthMetaData.ATTACHMENT_KEY);
        for( AuthMetaData authMetaData: allMetaData ) {
            if ( authMetaData != null ) {
                Collection<Config> authConfigs = authMetaData.getConfigurations();
                for ( Config config : authConfigs ) {
                    installAuthenticator(unit, config);
                }
            }
        }
	}

	@Override
	public void undeploy(DeploymentUnit arg0) {
		// TODO Auto-generated method stub

	}
	
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    private void installAuthenticator(DeploymentUnit unit, Config config) {
        String name     = config.getName();
        String domain   = config.getDomain();
        if (name != null && domain != null) {
            // Set up our authenticator
        	// TODO: What do I do instead of beanmetadatabuilders in as7? Hmmm.
//            String authenticatorBeanName = this.getApplicationName() + "-authentication-" + name;
//            BeanMetaDataBuilder authenticatorBuilder = BeanMetaDataBuilderFactory.createBuilder(authenticatorBeanName, UsersRolesAuthenticator.class.getName());
//            authenticatorBuilder.addPropertyMetaData("authDomain", domain);
//            BeanMetaData bmd = authenticatorBuilder.getBeanMetaData();
//            AttachmentUtils.attach(unit, bmd);
        }
    }
    
    private String applicationName;

}
