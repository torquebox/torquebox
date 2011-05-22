package org.torquebox.core.as;

import java.io.Closeable;
import java.io.IOException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.api.ServerDeploymentRepository;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.server.deployment.module.TempFileProviderService;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

public class KnobStructureProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        System.err.println( "Unit name: " + unit.getName() );

        if (!unit.getName().endsWith( ".knob" )) {
            return;
        }
        
        KnobDeploymentMarker.applyMark( unit );
        
        ModuleSpecification moduleSpec = unit.getAttachment( Attachments.MODULE_SPECIFICATION );
        moduleSpec.setChildFirst( true );

        // Until AS7-810 is implemented, we need to unmount and remount the root .knob
        // so it's mounted expanded
        remountExpanded( unit );
    }
    
    protected void remountExpanded(DeploymentUnit unit) throws DeploymentUnitProcessingException {
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        
        if (resourceRoot.getMountHandle() != null) {
            System.err.println( "Closing previous ResourceRoot before remounting" );
            VFSUtils.safeClose( resourceRoot.getMountHandle() );
        }
        
        try {
            final ServerDeploymentRepository serverDeploymentRepository = unit.getAttachment(Attachments.SERVER_DEPLOYMENT_REPOSITORY);
            final String deploymentName = unit.getName();
            final String deploymentRuntimeName = unit.getAttachment(Attachments.RUNTIME_NAME);
            //final byte[] deploymentHash = unit.getAttachment(Attachments.DEPLOYMENT_HASH);
            System.err.println( "CONTENTS: " + unit.getAttachment( Attachments.DEPLOYMENT_CONTENTS  ));
            
            System.err.println( "Remounting expanded: " + root );
            final Closeable closable = serverDeploymentRepository.mountDeploymentContent(deploymentName, deploymentRuntimeName, unit.getAttachment( Attachments.DEPLOYMENT_CONTENTS ), root, true);
            final MountHandle mountHandle = new MountHandle( closable );
            
            ResourceRoot expandedResourceRoot = new ResourceRoot( root, mountHandle );
            unit.putAttachment( Attachments.DEPLOYMENT_ROOT, expandedResourceRoot );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

}
