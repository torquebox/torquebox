package org.torquebox.auth;

import java.util.Collection;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController.Mode;
import org.torquebox.auth.AuthMetaData.Config;
import org.torquebox.auth.as.AuthServices;
import org.torquebox.core.app.RubyApplicationMetaData;

public class AuthDeployer implements DeploymentUnitProcessor {

	@Override
	public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
		DeploymentUnit unit = phaseContext.getDeploymentUnit();
		
		// We need the application name to name our bean with
        RubyApplicationMetaData appMetaData = unit.getAttachment(RubyApplicationMetaData.ATTACHMENT_KEY);
        if (appMetaData != null) {
            String applicationName = appMetaData.getApplicationName();
            this.setApplicationName(applicationName);

            // Install authenticators for every domain
            List<AuthMetaData> allMetaData = unit.getAttachmentList(AuthMetaData.ATTACHMENT_KEY);
            for( AuthMetaData authMetaData: allMetaData ) {
                if ( authMetaData != null ) {
                    Collection<Config> authConfigs = authMetaData.getConfigurations();
                    for ( Config config : authConfigs ) {
                        installAuthenticator(phaseContext, config);
                    }
                }
            }
        }
	}

	@Override
	public void undeploy(DeploymentUnit unit) {
		// TODO Clean up?
	}
	
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    private void installAuthenticator(DeploymentPhaseContext phaseContext, Config config) {
        String name     = config.getName();
        String domain   = config.getDomain();
        if (name != null && domain != null) {
            ServiceName serviceName = AuthServices.authenticationService( this.getApplicationName(), name );
            log.info( "Deploying Authenticator: " + serviceName );
            Authenticator authenticator = new Authenticator();
            authenticator.setAuthDomain(domain);
            ServiceBuilder<Authenticator> builder = phaseContext.getServiceTarget().addService( serviceName, authenticator );
            builder.setInitialMode( Mode.PASSIVE );
            builder.install();
        }
    }
    
    private String applicationName;
    private static final Logger log = Logger.getLogger( "org.torquebox.auth" );
}
