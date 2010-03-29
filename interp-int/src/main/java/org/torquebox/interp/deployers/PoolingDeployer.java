package org.torquebox.interp.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jruby.Ruby;
import org.torquebox.common.runtime.DefaultRubyRuntimePool;
import org.torquebox.common.runtime.SharedRubyRuntimePool;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.metadata.PoolingMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;

public class PoolingDeployer extends AbstractSimpleVFSRealDeployer<PoolingMetaData> {

	private static final String[] VALID_POOLS = { "web", "jobs", "queues", "messaging", "test1", "test2", "test3" };

	public PoolingDeployer() {
		super(PoolingMetaData.class);
		addOutput( BeanMetaData.class );
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, PoolingMetaData poolingMetaData) throws DeploymentException {
		log.info( "deploy pools for " + unit );
		for (String poolName : VALID_POOLS) {
			PoolMetaData poolMetaData = poolingMetaData.getPool(poolName);
			if (poolMetaData != null) {
				deploy(unit, poolMetaData);
			}
		}
	}

	protected void deploy(VFSDeploymentUnit unit, PoolMetaData poolMetaData) throws DeploymentException {

		String beanName = getBeanName(unit, poolMetaData.getName());

		BeanMetaData poolBean = null;

		if (poolMetaData.isGlobal()) {
			BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, SharedRubyRuntimePool.class
					.getName());
			builder.addPropertyMetaData( "name", poolMetaData.getName() );
			Ruby globalRuntime = unit.getAttachment(Ruby.class);
			builder.addConstructorParameter(Ruby.class.getName(), globalRuntime);
			poolBean = builder.getBeanMetaData();
		} else if (poolMetaData.isShared()) {
			BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, SharedRubyRuntimePool.class
					.getName());
			builder.addPropertyMetaData( "name", poolMetaData.getName() );
			ValueMetaData factoryInjection = builder.createInject("jboss.ruby.runtime.factory." + unit.getSimpleName());
			builder.addConstructorParameter(RubyRuntimeFactory.class.getName(), factoryInjection);
			poolBean = builder.getBeanMetaData();
		} else {
			BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(beanName, DefaultRubyRuntimePool.class.getName() );
			ValueMetaData factoryInjection = builder.createInject("jboss.ruby.runtime.factory." + unit.getSimpleName());
			builder.addConstructorParameter(RubyRuntimeFactory.class.getName(), factoryInjection);
			builder.addPropertyMetaData( "name", poolMetaData.getName() );
			builder.addPropertyMetaData("minInstances", poolMetaData.getMinimumSize());
			builder.addPropertyMetaData("maxInstances", poolMetaData.getMaximumSize());
			poolBean = builder.getBeanMetaData();
		}
		
		unit.addAttachment(BeanMetaData.class.getName() + "$" + beanName, poolBean, BeanMetaData.class);

	}

	public static String getBeanName(DeploymentUnit unit, String poolName) {
		return "torquebox.ruby.runtime.pool." + unit.getSimpleName() + "." + poolName;
	}

}
