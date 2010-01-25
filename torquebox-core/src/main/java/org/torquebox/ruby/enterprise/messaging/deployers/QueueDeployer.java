package org.torquebox.ruby.enterprise.messaging.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.ruby.enterprise.messaging.QueueMetaData;

public class QueueDeployer extends AbstractSimpleVFSRealDeployer<QueueMetaData> {

	public QueueDeployer() {
		super( QueueMetaData.class );
	}
	
	@Override
	public void deploy(VFSDeploymentUnit unit, QueueMetaData metadata) throws DeploymentException {
		
	}
	

}
