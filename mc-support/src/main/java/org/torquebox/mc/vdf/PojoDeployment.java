package org.torquebox.mc.vdf;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;

public class PojoDeployment {
	
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
		this.deployer.addDeployment( this.deployment );
		this.deployer.process();
	}
	
	public void stop() throws DeploymentException {
		this.deployer.undeploy( this.deployment );
	}
}
