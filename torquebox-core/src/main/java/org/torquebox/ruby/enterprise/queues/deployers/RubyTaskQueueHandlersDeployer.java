package org.torquebox.ruby.enterprise.queues.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.ruby.core.runtime.deployers.RubyRuntimePoolDeployer;
import org.torquebox.ruby.enterprise.queues.RubyTaskQueueHandler;
import org.torquebox.ruby.enterprise.queues.metadata.RubyTaskQueueMetaData;
import org.torquebox.ruby.enterprise.queues.metadata.RubyTaskQueuesMetaData;

public class RubyTaskQueueHandlersDeployer extends AbstractSimpleVFSRealDeployer<RubyTaskQueuesMetaData> {

	public RubyTaskQueueHandlersDeployer() {
		super(RubyTaskQueuesMetaData.class);
		addOutput(BeanMetaData.class);
	}

	public void deploy(VFSDeploymentUnit unit, RubyTaskQueuesMetaData queuesMetaData) throws DeploymentException {
		for (RubyTaskQueueMetaData queueMetaData : queuesMetaData.getQueues()) {
			deploy(unit, queueMetaData);
		}
	}

	public void deploy(VFSDeploymentUnit unit, RubyTaskQueueMetaData queueMetaData) throws DeploymentException {
		
		String beanName = "jboss.ruby.queue-handler." + unit.getSimpleName() + "." + queueMetaData.getQueueClassName();
		
		BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RubyTaskQueueHandler.class.getName() );
		
		String queueName = RubyTaskQueuesDeployer.getQueueName( unit, queueMetaData.getQueueClassName() );
		
		builder.addPropertyMetaData( "queueName", queueName );
		builder.addPropertyMetaData( "enabled", queueMetaData.isEnabled() );
		builder.addPropertyMetaData( "queueClassName", queueMetaData.getQueueClassName() );
		builder.addPropertyMetaData( "queueClassLocation", queueMetaData.getQueueClassLocation() );
		
		String runtimePoolName = RubyRuntimePoolDeployer.getBeanName( unit );
		ValueMetaData runtimePoolInject = builder.createInject( runtimePoolName );
		builder.addPropertyMetaData( "rubyRuntimePool", runtimePoolInject );
		
		builder.addDependency( RubyTaskQueuesDeployer.getObjectName( unit, queueMetaData.getQueueClassName() ) );
		
		unit.addAttachment( BeanMetaData.class.getName() + "$" + beanName, builder.getBeanMetaData(), BeanMetaData.class );

	}

}
