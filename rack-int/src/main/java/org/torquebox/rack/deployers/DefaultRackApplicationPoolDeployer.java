package org.torquebox.rack.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.metadata.RubyRackApplicationMetaData;

public class DefaultRackApplicationPoolDeployer extends AbstractDeployer {

	public DefaultRackApplicationPoolDeployer() {
		setInput(RubyRackApplicationMetaData.class);
		addInput(PoolMetaData.class);
		addOutput(PoolMetaData.class);
		setStage(DeploymentStages.DESCRIBE);
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		log.info(" deploy for " + unit);
		PoolMetaData poolMetaData = getPoolMetaData(unit, "web");
		if (poolMetaData != null) {
			return;
		}

		poolMetaData = new PoolMetaData();
		poolMetaData.setName("web");
		poolMetaData.setShared();

		AttachmentUtils.multipleAttach(unit, poolMetaData, "web");
	}

	protected static PoolMetaData getPoolMetaData(DeploymentUnit unit, String poolName) {
		for (PoolMetaData each : unit.getAllMetaData(PoolMetaData.class)) {
			if (each.getName().equals(poolName)) {
				return each;
			}
		}

		return null;
	}

}
