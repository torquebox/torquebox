package org.torquebox.messaging;

import org.jboss.as.ee.naming.ContextNames;
import org.jboss.as.ee.naming.RootContextService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.app.RubyApplicationMetaData;

public class ApplicationNamingContextBindingProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        if (appMetaData == null) {
            return;
        }

        RootContextService contextService = new RootContextService();
        ServiceName contextServiceName = ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( "queue" ).append( appMetaData.getApplicationName() );
        phaseContext.getServiceTarget().addService( contextServiceName, contextService )
                .install();
        
        RootContextService tasksService = new RootContextService();
        ServiceName tasksServiceName = contextServiceName.append(  "tasks"  );
        
        phaseContext.getServiceTarget().addService( tasksServiceName, tasksService )
            .addDependency(  contextServiceName )
            .install();

    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

}
