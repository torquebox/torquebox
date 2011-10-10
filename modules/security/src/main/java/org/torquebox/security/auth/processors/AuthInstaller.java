/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.security.auth.processors;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

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
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.security.auth.AuthMetaData;
import org.torquebox.security.auth.AuthMetaData.TorqueBoxAuthConfig;
import org.torquebox.security.auth.Authenticator;
import org.torquebox.security.auth.TorqueBoxLoginModule;
import org.torquebox.security.auth.as.AuthServices;
import org.torquebox.security.auth.as.AuthSubsystemAdd;

public class AuthInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        // We need the application name to name our bean with
        RubyAppMetaData appMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        if (appMetaData != null) {
            String applicationName = appMetaData.getApplicationName();
            this.setApplicationName( applicationName );

            // Install authenticators for every domain
            List<AuthMetaData> allMetaData = unit.getAttachmentList( AuthMetaData.ATTACHMENT_KEY );
            for (AuthMetaData authMetaData : allMetaData) {
                if (authMetaData != null) {
                    Collection<TorqueBoxAuthConfig> authConfigs = authMetaData.getConfigurations();
                    for (TorqueBoxAuthConfig config : authConfigs) {
                        installAuthenticator( phaseContext, config );
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

    private String getTorqueBoxDomainServiceName() {
        return AuthSubsystemAdd.TORQUEBOX_DOMAIN + "-" + this.getApplicationName();
    }

    private void addTorqueBoxSecurityDomainService(DeploymentPhaseContext context, TorqueBoxAuthConfig config) {
        String domain = this.getTorqueBoxDomainServiceName();
        final ApplicationPolicy applicationPolicy = new ApplicationPolicy( domain );
        AuthenticationInfo authenticationInfo = new AuthenticationInfo( domain );

        // TODO: Can we feed usernames/passwords into the options hash?
        Map<String, Object> options = new HashMap<String, Object>();
        Map<String, String> credentials = config.getCredentials();
        if (credentials != null) {
            options.put( "credentials", credentials );
        }
        AppConfigurationEntry entry = new AppConfigurationEntry( TorqueBoxLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, options );
        authenticationInfo.addAppConfigurationEntry( entry );
        applicationPolicy.setAuthenticationInfo( authenticationInfo );

        // TODO: Do we need to bother with a JSSESecurityDomain? Null in this
        // case may be OK
        // TODO: Null cache type?
        final SecurityDomainService securityDomainService = new SecurityDomainService( domain, applicationPolicy, null, null );
        final ServiceTarget target = context.getServiceTarget();

        ServiceBuilder<SecurityDomainContext> builder = target
                .addService( SecurityDomainService.SERVICE_NAME.append( domain ), securityDomainService )
                .addDependency( SecurityManagementService.SERVICE_NAME, ISecurityManagement.class,
                        securityDomainService.getSecurityManagementInjector() )
                .addDependency( JaasConfigurationService.SERVICE_NAME, Configuration.class,
                        securityDomainService.getConfigurationInjector() );

        builder.setInitialMode( Mode.ON_DEMAND ).install();
    }

    private void installAuthenticator(DeploymentPhaseContext phaseContext, TorqueBoxAuthConfig config) {
        String name = config.getName();
        String domain = config.getDomain();
        if (name != null && domain != null) {
            if (domain.equals( AuthSubsystemAdd.TORQUEBOX_DOMAIN )) {
                // activate the service
                log.debug( "Activating SecurityDomainService for " + domain );
                ServiceController<?> torqueboxService = phaseContext.getServiceRegistry().getService(
                        SecurityDomainService.SERVICE_NAME.append( AuthSubsystemAdd.TORQUEBOX_DOMAIN ) );
                if (torqueboxService != null)
                    torqueboxService.setMode( Mode.ACTIVE );
            } else if (domain.equals( this.getTorqueBoxDomainServiceName() )) {
                // activate the service
                this.addTorqueBoxSecurityDomainService( phaseContext, config );
                log.debug( "Activating SecurityDomainService for " + domain );
                ServiceController<?> torqueboxService = phaseContext.getServiceRegistry().getService(
                        SecurityDomainService.SERVICE_NAME.append( this.getTorqueBoxDomainServiceName() ) );
                if (torqueboxService != null) {
                    torqueboxService.setMode( Mode.ACTIVE );
                }
            }
            ServiceName serviceName = AuthServices.authenticationService( this.getApplicationName(), name );
            log.debug( "Deploying Authenticator: " + serviceName + " for security domain: " + domain );
            Authenticator authenticator = new Authenticator();
            authenticator.setAuthDomain( domain );
            ServiceBuilder<Authenticator> builder = phaseContext.getServiceTarget().addService( serviceName, authenticator );
            builder.setInitialMode( Mode.PASSIVE );
            builder.install();
        }
    }

    private String applicationName;
    private static final Logger log = Logger.getLogger( "org.torquebox.auth" );
}
