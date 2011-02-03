package org.torquebox.base.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;

public abstract class AbstractRecognizer extends AbstractDeployer {

    public AbstractRecognizer() {
        setStage(DeploymentStages.PRE_PARSE);
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy((VFSDeploymentUnit) unit);
        }
    }
    
    protected abstract boolean isRecognized(VFSDeploymentUnit unit);
    protected abstract void handle(VFSDeploymentUnit unit) throws DeploymentException;

    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        if (isRecognized(unit) ) {
            handle( unit );
        }
    }


    protected static boolean hasAnyOf(VirtualFile root, String... paths) {
        for (String path : paths) {
            if (root.getChild(path).exists()) {
                return true;
            }
        }
        return false;
    }

}
