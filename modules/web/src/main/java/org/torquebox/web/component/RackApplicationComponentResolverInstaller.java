package org.torquebox.web.component;

import java.io.IOException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.component.ComponentEval;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.web.as.WebServices;
import org.torquebox.web.rack.RackApplicationMetaData;

import com.allen_sauer.gwt.log.client.Log;

public class RackApplicationComponentResolverInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rackAppMetaData == null) {
            return;
        }

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        ComponentEval instantiator = new ComponentEval();
        try {
            instantiator.setCode( getCode( rackAppMetaData.getRackUpScript( root ) ) );
            instantiator.setLocation( rackAppMetaData.getRackUpScriptFile( root ).toURL().toString() );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }

        ServiceName serviceName = WebServices.rackApplicationComponentResolver( unit );
        ComponentResolver resolver = new ComponentResolver();
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( RackApplicationComponent.class );
        
        log.info( "Installing Rack app component resolver: " + serviceName );
        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.PASSIVE );
        builder.install();
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
