package org.torquebox.mc.vdf;

import java.io.IOException;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.util.automount.Automounter;
import org.jboss.vfs.util.automount.MountOption;

public class PojoDeployment {

    private static final Logger log = Logger.getLogger(PojoDeployment.class);

    private DeployerClient deployer;
    private VFSDeployment deployment;

    public PojoDeployment(DeployerClient deployer, VFSDeployment deployment) {
        this.deployer = deployer;
        this.deployment = deployment;
    }

    public VFSDeployment getDeployment() {
        return this.deployment;
    }

    public DeployerClient getMainDeployer() {
        return this.deployer;
    }

    public void start() throws DeploymentException {
        VirtualFile root = deployment.getRoot();

        if (!root.isDirectory()) {
            try {
                Automounter.mount(root, MountOption.COPY);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }

        this.deployer.addDeployment(this.deployment);
        this.deployer.process();
    }

    public void stop() throws DeploymentException {
        try {
            this.deployer.undeploy(this.deployment);
        } finally {
            VirtualFile root = deployment.getRoot();
            Automounter.cleanup(root);
        }

    }
}
