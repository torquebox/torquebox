package org.torquebox.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import org.jboss.as.security.ModulesMap;
import org.jboss.as.security.plugins.SecurityDomainContext;
import org.jboss.as.security.service.JaasConfigurationService;
import org.jboss.as.security.service.SecurityDomainService;
import org.jboss.as.security.service.SecurityManagementService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.torquebox.auth.AuthMetaData.Config;
import org.torquebox.auth.as.AuthServices;
import org.torquebox.core.app.RubyApplicationMetaData;

public class AuthDeployer implements DeploymentUnitProcessor {

	private static final String TORQUEBOX_DOMAIN = "torquebox";
	
	@Override
	public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
		DeploymentUnit unit = phaseContext.getDeploymentUnit();
		
		// We need the application name to name our bean with
        RubyApplicationMetaData appMetaData = unit.getAttachment(RubyApplicationMetaData.ATTACHMENT_KEY);
        if (appMetaData != null) {
            String applicationName = appMetaData.getApplicationName();
            this.setApplicationName(applicationName);

            // TODO: Only create this service if the 'torquebox' domain is specified
//            buildTorqueBoxDomain(phaseContext);
            
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

	private void buildTorqueBoxDomain(DeploymentPhaseContext phaseContext) {
		final ApplicationPolicy applicationPolicy = new ApplicationPolicy(TORQUEBOX_DOMAIN);
		AuthenticationInfo authenticationInfo = new AuthenticationInfo(TORQUEBOX_DOMAIN);
		
		// TODO: Can we feed usernames/passwords into the options hash?
		Map<String, Object> options = new HashMap<String, Object>();
		// TODO: Create/Use torquebox login module
		AppConfigurationEntry entry = new AppConfigurationEntry(ModulesMap.AUTHENTICATION_MAP.get("Simple"), LoginModuleControlFlag.REQUIRED, options);
		authenticationInfo.addAppConfigurationEntry(entry);
		applicationPolicy.setAuthenticationInfo(authenticationInfo);
		
		// TODO: Do we need to bother with a JSSESecurityDomain? Null in this case may be OK
		// TODO: Null cache type?
		final SecurityDomainService securityDomainService = new SecurityDomainService(TORQUEBOX_DOMAIN, applicationPolicy, null, null); 
		final ServiceTarget target = phaseContext.getServiceTarget();
		
		ServiceBuilder<SecurityDomainContext> builder = target
		.addService(SecurityDomainService.SERVICE_NAME.append(TORQUEBOX_DOMAIN), securityDomainService)
		.addDependency(SecurityManagementService.SERVICE_NAME, ISecurityManagement.class,
		        securityDomainService.getSecurityManagementInjector())
		.addDependency(JaasConfigurationService.SERVICE_NAME, Configuration.class,
		        securityDomainService.getConfigurationInjector());
		
		builder.setInitialMode(ServiceController.Mode.ACTIVE).install();
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
