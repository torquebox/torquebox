package org.torquebox.rack.deployers;

import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileVisitor;
import org.jboss.vfs.VisitorAttributes;
import org.torquebox.rack.metadata.RackApplicationMetaData;

public class RackApplicationExploder extends AbstractDeployer {

    public RackApplicationExploder() {
        setStage(DeploymentStages.POST_PARSE);
        setInput(RackApplicationMetaData.class);
        addOutput(RackApplicationMetaData.class);
        setRelativeOrder( -1000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RackApplicationMetaData metaData = unit.getAttachment(RackApplicationMetaData.class);

        VirtualFile rackRoot = metaData.getRackRoot();

        try {
            VirtualFile explodedRackRoot = getExplodedApplication(rackRoot);
            if (!rackRoot.equals(explodedRackRoot)) {
                metaData.explode(explodedRackRoot);
            }
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * This method is a hack to make sure the WAR is fully exploded. Currently
     * this is only needed for WARs that come through the DeclaredStructure
     * deployer. This should be removed when the DeclaredStructure deployer
     * correctly support exploding WARs.
     */
    private VirtualFile getExplodedApplication(VirtualFile virtualFile) throws IOException {
        if (virtualFile.isDirectory()) {
            VirtualFileVisitor visitor = new VirtualFileVisitor() {
                public void visit(VirtualFile virtualFile) {
                    try {
                        virtualFile.getPhysicalFile();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to force explosion of VirtualFile: " + virtualFile, e);
                    }
                }

                public VisitorAttributes getAttributes() {
                    return VisitorAttributes.RECURSE_LEAVES_ONLY;
                }
            };
            virtualFile.visit(visitor);
        }
        return VFS.getChild(virtualFile.getPhysicalFile().getAbsolutePath());
    }

}
