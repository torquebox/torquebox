package org.torquebox.messaging.component;

import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServer;

import org.jboss.as.jmx.MBeanRegistrationService;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.jmx.ObjectNameFactory;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.DeploymentNotifier;
import org.torquebox.core.component.BaseRubyComponentDeployer;
import org.torquebox.core.component.ComponentClass;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.messaging.as.MessagingServices;

public class MessageProcessorComponentResolverInstaller extends BaseRubyComponentDeployer {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<MessageProcessorMetaData> allMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY );

        if (allMetaData == null || allMetaData.isEmpty()) {
            return;
        }

        for (MessageProcessorMetaData each : allMetaData) {
            deploy( phaseContext, each );

        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, final MessageProcessorMetaData metaData) throws DeploymentUnitProcessingException {

        log.info( "DEPLOY: " + metaData );

        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ComponentClass instantiator = new ComponentClass();

        instantiator.setClassName( metaData.getRubyClassName() );
        instantiator.setRequirePath( metaData.getRubyRequirePath() );

        ServiceName serviceName = MessagingServices.messageProcessorComponentResolver( unit, metaData.getName() );
        ComponentResolver resolver = createComponentResolver( unit );
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( MessageProcessorComponent.class );

        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.ON_DEMAND );
        addInjections( phaseContext, resolver, getInjectionPathPrefixes( phaseContext, metaData.getRubyRequirePath() ), builder );
        builder.install();

        // Add to our notifier's watch list
        unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName );
        

    }

    protected List<String> getInjectionPathPrefixes(DeploymentPhaseContext phaseContext, String requirePath) {

        final List<String> prefixes = defaultInjectionPathPrefixes();

        if (requirePath != null) {

            final DeploymentUnit unit = phaseContext.getDeploymentUnit();
            final ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
            final VirtualFile root = resourceRoot.getRoot();

            final String sourcePath = searchForSourceFile( root, requirePath, true, true, "app/tasks", "app/processors", "lib" );

            if (sourcePath != null) {
                prefixes.add( sourcePath );
            }
        }

        return prefixes;
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.component" );
}
