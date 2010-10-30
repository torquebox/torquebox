package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.core.ManagedQueue;
import org.torquebox.messaging.metadata.QueueMetaData;


/**
 * <pre>
 * Stage: REAL
 *    In: QueueMetaData
 *   Out: ManagedQueue
 * </pre>
 *
 */
public class ManagedQueueDeployer extends AbstractDeployer {
	
	public ManagedQueueDeployer() {
		setAllInputs( true );
		addOutput(BeanMetaData.class);
		setStage( DeploymentStages.REAL );
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );
		
		for ( QueueMetaData each : allMetaData) {
			deploy( unit, each );
		}
	}


	protected void deploy(DeploymentUnit unit, QueueMetaData metaData) {
		String beanName = AttachmentUtils.beanName( unit, ManagedQueue.class, metaData.getName() );
		
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName, ManagedQueue.class.getName() );
		builder.addPropertyMetaData( "name", metaData.getName() );
		
		ValueMetaData hornetServerInjection = builder.createInject("JMSServerManager" );
		builder.addPropertyMetaData( "server", hornetServerInjection );
		
		AttachmentUtils.attach( unit, builder.getBeanMetaData() );
	}	

}
