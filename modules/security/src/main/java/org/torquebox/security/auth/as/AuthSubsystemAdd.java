/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.security.auth.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.security.ModulesMap;
import org.jboss.as.security.plugins.SecurityDomainContext;
import org.jboss.as.security.service.JaasConfigurationService;
import org.jboss.as.security.service.SecurityDomainService;
import org.jboss.as.security.service.SecurityManagementService;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.torquebox.security.as.SecurityDependencyProcessor;
import org.torquebox.security.as.SecurityExtension;
import org.torquebox.security.auth.processors.AuthDefaultsProcessor;
import org.torquebox.security.auth.processors.AuthInstaller;
import org.torquebox.security.auth.processors.AuthYamlParsingProcessor;

public class AuthSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) {
        model.setEmptyObject();
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {

        context.addStep( new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                addDeploymentProcessors( processorTarget );
            }
        }, OperationContext.Stage.RUNTIME );

        addTorqueBoxSecurityDomainService( context, verificationHandler, newControllers );
    }

    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget) {
        processorTarget.addDeploymentProcessor( SecurityExtension.SUBSYSTEM_NAME, Phase.PARSE, 15, new AuthYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( SecurityExtension.SUBSYSTEM_NAME, Phase.PARSE, 20, new AuthDefaultsProcessor() );
        processorTarget.addDeploymentProcessor( SecurityExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, 3, new SecurityDependencyProcessor() );
        processorTarget.addDeploymentProcessor( SecurityExtension.SUBSYSTEM_NAME, Phase.INSTALL, 0, new AuthInstaller() );
    }

    protected void addTorqueBoxSecurityDomainService(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {
        final ApplicationPolicy applicationPolicy = new ApplicationPolicy( TORQUEBOX_DOMAIN );
        AuthenticationInfo authenticationInfo = new AuthenticationInfo( TORQUEBOX_DOMAIN );

        // TODO: Can we feed usernames/passwords into the options hash?
        Map<String, Object> options = new HashMap<String, Object>();
        AppConfigurationEntry entry = new AppConfigurationEntry( ModulesMap.AUTHENTICATION_MAP.get( "Simple" ), LoginModuleControlFlag.REQUIRED, options );
        authenticationInfo.addAppConfigurationEntry( entry );
        applicationPolicy.setAuthenticationInfo( authenticationInfo );

        // TODO: Do we need to bother with a JSSESecurityDomain? Null in this
        // case may be OK
        // TODO: Null cache type?
        final SecurityDomainService securityDomainService = new SecurityDomainService( TORQUEBOX_DOMAIN, applicationPolicy, null, null );
        final ServiceTarget target = context.getServiceTarget();

        ServiceBuilder<SecurityDomainContext> builder = target
                .addService( SecurityDomainService.SERVICE_NAME.append( TORQUEBOX_DOMAIN ), securityDomainService )
                .addDependency( SecurityManagementService.SERVICE_NAME, ISecurityManagement.class,
                        securityDomainService.getSecurityManagementInjector() )
                .addDependency( JaasConfigurationService.SERVICE_NAME, Configuration.class,
                        securityDomainService.getConfigurationInjector() )
                .addListener( verificationHandler );

        newControllers.add( builder.setInitialMode( Mode.ON_DEMAND ).install() );
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    public static final String TORQUEBOX_DOMAIN = "torquebox";
    public static final AuthSubsystemAdd ADD_INSTANCE = new AuthSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.auth.as" );
}
