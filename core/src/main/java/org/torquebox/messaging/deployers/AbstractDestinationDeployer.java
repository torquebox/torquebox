package org.torquebox.messaging.deployers;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.messaging.Destination;
import org.torquebox.messaging.metadata.DestinationMetaData;
import org.torquebox.messaging.metadata.DestinationsMetaData;

public abstract class AbstractDestinationDeployer<S extends DestinationMetaData, P extends DestinationsMetaData<S>>
		extends AbstractSimpleVFSRealDeployer<P> {

	protected static final String SERVER_PEER_NAME = "jboss.messaging:service=ServerPeer";

	private Class<? extends Destination> destinationClass;

	public AbstractDestinationDeployer(Class<P> cls) {
		super(cls);
		setRelativeOrder( 1000 );
	}
	
	protected Class<? extends Destination> getDestinationClass() {
		return this.destinationClass;
	}
	
	protected void setDestinationClass(Class<? extends Destination> destinationClass) {
		this.destinationClass = destinationClass;
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, P metaData) throws DeploymentException {
		for (S destinationMetaData : metaData.getDestinations()) {
			deploy(unit, destinationMetaData);
		}
	}
	
	protected void deploy(VFSDeploymentUnit unit, S destinationMetaData) throws DeploymentException {

		log.info("Deploy for " + this.destinationClass + " [" + destinationMetaData.getName() + "]");
		
		String beanName = getBeanName( destinationMetaData.getName() );
		
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName, getDestinationClass().getName() );
		builder.addPropertyMetaData( "name", destinationMetaData.getName() );
		
		ValueMetaData hornetServerInjection = builder.createInject("JMSServerManager" );
		builder.addPropertyMetaData( "server", hornetServerInjection );
		
		BeanMetaData beanMetaData = builder.getBeanMetaData();

		unit.addAttachment(BeanMetaData.class.getName() + "$" + getDestinationClass().getName() + "$" + beanName, beanMetaData,
				BeanMetaData.class);
	}
	
	public static String getBeanName(String destinationName) {
		String beanName = "torquebox.messaging.destination." + destinationName;
		return beanName;
	}

}
