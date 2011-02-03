package org.torquebox.base.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;

public abstract class AbstractParsingDeployer extends AbstractDeployer {

    protected VirtualFile getMetaDataFile(VFSDeploymentUnit unit, String fileName) {
        VirtualFile metaDataFile = unit.getAttachment(fileName + ".altDD", VirtualFile.class);

        if (metaDataFile == null) {
            metaDataFile = unit.getMetaDataFile(fileName);
        }

        return metaDataFile;
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy((VFSDeploymentUnit) unit);
        } else {
            throw new DeploymentException( "Deployer only accepts VFS deployment units." );
        }
    }
    
    protected abstract void deploy(VFSDeploymentUnit unit) throws DeploymentException;
}
