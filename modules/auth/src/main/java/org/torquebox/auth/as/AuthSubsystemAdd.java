package org.torquebox.auth.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.ModelAddOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.as.security.ModulesMap;
import org.jboss.as.security.plugins.SecurityDomainContext;
import org.jboss.as.security.service.JaasConfigurationService;
import org.jboss.as.security.service.SecurityDomainService;
import org.jboss.as.security.service.SecurityManagementService;
import org.jboss.as.server.BootOperationContext;
import org.jboss.as.server.BootOperationHandler;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.torquebox.auth.AuthDefaultsProcessor;
import org.torquebox.auth.AuthDeployer;
import org.torquebox.auth.AuthYamlParsingProcessor;

public class AuthSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {
        final ModelNode subModel = context.getSubModel();
        subModel.setEmptyObject();

        if (!handleBootContext( context, resultHandler )) {
            resultHandler.handleResultComplete();
        }
        return compensatingResult( operation );
    }

    protected boolean handleBootContext(final OperationContext operationContext, final ResultHandler resultHandler) {

        if (!(operationContext instanceof BootOperationContext)) {
            return false;
        }

        final BootOperationContext context = (BootOperationContext) operationContext;

        context.getRuntimeContext().setRuntimeTask( bootTask( context, resultHandler ) );
        return true;
    }

    protected void addDeploymentProcessors(final BootOperationContext context) {
        context.addDeploymentProcessor( Phase.PARSE, 0, new AuthYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 20, new AuthDefaultsProcessor() );
        context.addDeploymentProcessor( Phase.DEPENDENCIES, 3, new AuthDependencyProcessor() );
        context.addDeploymentProcessor( Phase.INSTALL, 0, new AuthDeployer() );
    }

    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                addDeploymentProcessors( bootContext );
                addTorqueBoxSecurityDomainService( context );
                resultHandler.handleResultComplete();
            }
        };
    }

    protected BasicOperationResult compensatingResult(ModelNode operation) {
        final ModelNode compensatingOperation = new ModelNode();
        compensatingOperation.get( OP ).set( REMOVE );
        compensatingOperation.get( OP_ADDR ).set( operation.get( OP_ADDR ) );
        return new BasicOperationResult( compensatingOperation );
    }

    protected void addTorqueBoxSecurityDomainService(RuntimeTaskContext context) {
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
                        securityDomainService.getConfigurationInjector() );

        builder.setInitialMode( Mode.ON_DEMAND ).install();
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    public static final String TORQUEBOX_DOMAIN = "torquebox";
    static final AuthSubsystemAdd ADD_INSTANCE = new AuthSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.auth.as" );
}
