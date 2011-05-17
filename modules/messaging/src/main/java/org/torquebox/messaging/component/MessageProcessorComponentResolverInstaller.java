package org.torquebox.messaging.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        List<MessageProcessorMetaData> allMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENT_KEY );
        
        if ( allMetaData == null || allMetaData.isEmpty() ) {
            return;
        }

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        
        for ( MessageProcessorMetaData each : allMetaData ) {
            deploy( phaseContext, each);
            
        }
    }
    
    protected void deploy(DeploymentPhaseContext phaseContext, MessageProcessorMetaData metaData) throws DeploymentUnitProcessingException {
        
        log.info(  "DEPLOY: " + metaData  );
        
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        ComponentClass instantiator = new ComponentClass();

        instantiator.setClassName( metaData.getRubyClassName() );
        instantiator.setRequirePath( metaData.getRubyRequirePath() );

        ServiceName serviceName = MessagingServices.messageProcessorComponentResolver( unit, metaData.getName() );
        ComponentResolver resolver = new ComponentResolver();
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( MessageProcessorComponent.class );
        
        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.ON_DEMAND );
        addInjections( phaseContext, resolver, builder );
        builder.install();
        
        // Add to our notifier's watch list
        unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName );
    }
    

    @Override
    protected List<String> getInjectionPathPrefixes(DeploymentPhaseContext phaseContext) {
        
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<MessageProcessorMetaData> allMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENT_KEY );
        
        List<String> prefixes = new ArrayList<String>();
        
        for ( MessageProcessorMetaData each : allMetaData ) {
            prefixes.add( each.getRubyRequirePath() );
            prefixes.add( "lib/" );
        }
        return prefixes;
    }


    protected String getCode(String rackupScript) {
        return "require %q(rack)\nRack::Builder.new{(\n" + rackupScript + "\n)}.to_app";
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.component" );
}
