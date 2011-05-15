package org.torquebox.core.as;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.SubDeploymentMarker;
import org.jboss.as.server.deployment.module.AdditionalModuleSpecification;
import org.jboss.as.server.deployment.module.ModuleRootMarker;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.server.deployment.module.TempFileProviderService;
import org.jboss.logging.Logger;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;

public class KnobStructureProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        if (!unit.getName().endsWith( ".knob" )) {
            return;
        }
        
        KnobDeploymentMarker.applyMark( unit );
        
        ModuleSpecification moduleSpec = unit.getAttachment( Attachments.MODULE_SPECIFICATION );
        moduleSpec.setChildFirst( true );

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        try {

            List<ResourceRoot> children = new ArrayList<ResourceRoot>();
            
            for (String scanRoot : SCAN_ROOTS) {
                for (VirtualFile child : getJarFiles( root.getChild( scanRoot ) )) {
                    final Closeable closable = child.isFile() ? mount( child, false ) : null;
                    final MountHandle mountHandle = new MountHandle( closable );
                    final ResourceRoot childResource = new ResourceRoot( child, mountHandle );
                    ModuleRootMarker.mark(childResource);
                    unit.addToAttachmentList( Attachments.RESOURCE_ROOTS, childResource );
                    children.add( childResource );
                }
            }
            
            ModuleIdentifier deploymentModuleIdentifier = unit.getAttachment( Attachments.MODULE_IDENTIFIER );
            ModuleIdentifier jarsModuleIdentifier = ModuleIdentifier.create( deploymentModuleIdentifier.getName() + "-jars" );
            AdditionalModuleSpecification jarsModule = new AdditionalModuleSpecification( jarsModuleIdentifier, children );
            unit.addToAttachmentList( Attachments.ADDITIONAL_MODULES, jarsModule );
        } catch (IOException e) {
            log.error( "Error processing jars", e );
        }

    }

    public List<VirtualFile> getJarFiles(VirtualFile dir) throws IOException {
        return dir.getChildrenRecursively( JAR_FILTER );
    }

    private static Closeable mount(VirtualFile moduleFile, boolean explode) throws IOException {
        return explode ? VFS.mountZipExpanded( moduleFile, moduleFile, TempFileProviderService.provider() )
                       : VFS.mountZip( moduleFile, moduleFile, TempFileProviderService.provider() );
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private static final List<String> SCAN_ROOTS = new ArrayList<String>() {
        {
            add( "lib" );
            add( "vendor/jars" );
            add( "vendor/plugins" );
        }
    };

    public static final VirtualFileFilter JAR_FILTER = new SuffixMatchFilter( ".jar", VisitorAttributes.DEFAULT );

    private static final Logger log = Logger.getLogger( "org.torquebox.core.as" );

}
