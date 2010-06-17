package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;

public class MessagingRuntimePoolDeployer extends AbstractDeployer {

	private String instanceFactoryName;

	public MessagingRuntimePoolDeployer() {
		setStage(DeploymentStages.PRE_REAL);
		addInput(PoolMetaData.class);
		addOutput(PoolMetaData.class);
	}
	
	public void setInstanceFactoryName(String instanceFactoryName) {
		this.instanceFactoryName = instanceFactoryName;
	}
	
	public String getInstanceFactoryName() {
		return this.instanceFactoryName;
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		log.info("Checking for deployment");
		if (unit.getAllMetaData(MessageProcessorMetaData.class).isEmpty()) {
			return;
		}

		Set<? extends PoolMetaData> allPools = unit.getAllMetaData(PoolMetaData.class);

		PoolMetaData pool = null;

		for (PoolMetaData each : allPools) {
			if (each.getName().equals("messaging")) {
				pool = each;
				break;
			}
		}

		if (pool == null) {
			log.debug("no pool configured yet");
			pool = new PoolMetaData();
			pool.setName("messaging");
			pool.setShared();
			pool.setInstanceFactoryName( this.instanceFactoryName );
			log.debug( "configured pool: " + pool );
			AttachmentUtils.multipleAttach(unit, pool, "messaging");
		}
	}

}
