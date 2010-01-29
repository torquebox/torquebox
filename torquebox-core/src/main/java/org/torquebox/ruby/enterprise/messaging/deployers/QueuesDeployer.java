package org.torquebox.ruby.enterprise.messaging.deployers;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.jms.server.destination.QueueService;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;
import org.jboss.system.metadata.ServiceValueMetaData;
import org.torquebox.ruby.enterprise.messaging.QueueMetaData;
import org.torquebox.ruby.enterprise.messaging.QueuesMetaData;

public class QueuesDeployer extends AbstractSimpleVFSRealDeployer<QueuesMetaData> {

	private static final String SERVER_PEER_NAME = "jboss.messaging:service=ServerPeer";

	public QueuesDeployer() {
		super(QueuesMetaData.class);
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, QueuesMetaData metaData) throws DeploymentException {
		for (QueueMetaData queueMetaData : metaData.getQueues()) {
			deploy(unit, queueMetaData);
		}
	}

	protected void deploy(VFSDeploymentUnit unit, QueueMetaData queueMetaData) throws DeploymentException {

		log.info("Deploy for queue [" + queueMetaData.getName() + "]");
		ServiceMetaData metaData = new ServiceMetaData();

		metaData.setCode(QueueService.class.getName());

		String objectName = getObjectName( queueMetaData.getName() );
		
		try {
			metaData.setObjectName(new ObjectName(objectName) );
		} catch (MalformedObjectNameException e) {
			throw new DeploymentException(e);
		}

		metaData.setXMBeanDD("xmdesc/Queue-xmbean.xml");

		ServiceConstructorMetaData constructorMetaData = new ServiceConstructorMetaData();
		constructorMetaData.setSignature(new String[] { Boolean.TYPE.getName() });
		constructorMetaData.setParameters(new Object[] { Boolean.TRUE });
		metaData.setConstructor(constructorMetaData);

		ServiceValueMetaData serverPeerVal = new ServiceTextValueMetaData(SERVER_PEER_NAME);

		ServiceAttributeMetaData serverPeerAttr = new ServiceAttributeMetaData();
		serverPeerAttr.setName("ServerPeer");
		serverPeerAttr.setValue(serverPeerVal);

		metaData.addAttribute(serverPeerAttr);

		ServiceValueMetaData clusteredVal = new ServiceTextValueMetaData("true");
		ServiceAttributeMetaData clusteredAttr = new ServiceAttributeMetaData();
		clusteredAttr.setName("Clustered");
		clusteredAttr.setValue(clusteredVal);

		metaData.addAttribute(clusteredAttr);

		try {
			ServiceDependencyMetaData serverPeerDep = new ServiceDependencyMetaData();
			serverPeerDep.setIDependOnObjectName(new ObjectName(SERVER_PEER_NAME));
			metaData.addDependency(serverPeerDep);
		} catch (MalformedObjectNameException e) {
			throw new DeploymentException(e);
		}

		unit.addAttachment(ServiceMetaData.class.getName() + "$QueueService$" + objectName, metaData,
				ServiceMetaData.class);
	}
	
	public static String getObjectName(String queueName) {
		return "jboss.messaging.destination:service=Queue,name=" + queueName;
	}

}
