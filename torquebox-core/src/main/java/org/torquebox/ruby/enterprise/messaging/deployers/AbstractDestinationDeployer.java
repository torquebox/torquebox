package org.torquebox.ruby.enterprise.messaging.deployers;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.system.metadata.ServiceMetaData;
import org.torquebox.ruby.enterprise.messaging.DestinationMetaData;
import org.torquebox.ruby.enterprise.messaging.DestinationsMetaData;
import org.torquebox.ruby.enterprise.messaging.Queue;

public abstract class AbstractDestinationDeployer<S extends DestinationMetaData, P extends DestinationsMetaData<S>>
		extends AbstractSimpleVFSRealDeployer<P> {

	protected static final String SERVER_PEER_NAME = "jboss.messaging:service=ServerPeer";

	private String service;

	public AbstractDestinationDeployer(Class<P> cls) {
		super(cls);
	}
	
	protected String getService() {
		return this.service;
	}
	
	protected void setService(String service) {
		this.service = service;
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, P metaData) throws DeploymentException {
		for (S destinationMetaData : metaData.getDestinations()) {
			deploy(unit, destinationMetaData);
		}
	}
	
	protected void deploy(VFSDeploymentUnit unit, S destinationMetaData) throws DeploymentException {

		log.info("Deploy for " + this.service + " [" + destinationMetaData.getName() + "]");
		
		String objectName = "torquebox.queue." + destinationMetaData.getName();
		
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( objectName, Queue.class.getName() );
		
		builder.addPropertyMetaData( "name", destinationMetaData.getName() );
		ValueMetaData hornetServerInjection = builder.createInject("HornetQServer", "hornetQServerControl");
		builder.addPropertyMetaData( "server", hornetServerInjection );
		
		BeanMetaData beanMetaData = builder.getBeanMetaData();

		unit.addAttachment(BeanMetaData.class.getName() + "$" + objectName, beanMetaData,
				BeanMetaData.class);
	}

}
