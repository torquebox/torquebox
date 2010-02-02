package org.torquebox.ruby.enterprise.messaging.deployers;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;
import org.jboss.system.metadata.ServiceValueMetaData;
import org.torquebox.ruby.enterprise.messaging.DestinationMetaData;
import org.torquebox.ruby.enterprise.messaging.DestinationsMetaData;

public abstract class AbstractDestinationDeployer<S extends DestinationMetaData, P extends DestinationsMetaData<S>>
		extends AbstractSimpleVFSRealDeployer<P> {

	protected static final String SERVER_PEER_NAME = "jboss.messaging:service=ServerPeer";

	private String service;
	private String xmbeanDd;
	private String code;

	public AbstractDestinationDeployer(Class<P> cls) {
		super(cls);
	}
	
	protected String getService() {
		return this.service;
	}
	
	protected void setService(String service) {
		this.service = service;
	}

	protected String getXMBeanDD() {
		if ( this.xmbeanDd != null ) {
			return this.xmbeanDd;
		}
		
		return "xmdesc/" + this.service + "-xmbean.xml";
	}
	
	protected void setXMBeanDD(String xmbeanDd) {
		this.xmbeanDd = xmbeanDd;
	}
	
	protected String getCode() {
		return this.code;
	}
	
	protected void setCode(String code) {
		this.code = code;
	}
	
	protected String getObjectName(String name) {
		return "jboss.messaging.destination:service=" + this.service + ",name=" + name;
	}


	@Override
	public void deploy(VFSDeploymentUnit unit, P metaData) throws DeploymentException {
		for (S destinationMetaData : metaData.getDestinations()) {
			deploy(unit, destinationMetaData);
		}
	}
	
	protected void deploy(VFSDeploymentUnit unit, S destinationMetaData) throws DeploymentException {

		log.info("Deploy for " + this.service + " [" + destinationMetaData.getName() + "]");
		ServiceMetaData metaData = new ServiceMetaData();

		metaData.setCode( getCode() );

		String objectName = getObjectName(destinationMetaData.getName());

		try {
			metaData.setObjectName(new ObjectName(objectName));
		} catch (MalformedObjectNameException e) {
			throw new DeploymentException(e);
		}

		metaData.setXMBeanDD( getXMBeanDD() );

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

		unit.addAttachment(ServiceMetaData.class.getName() + "$" + objectName, metaData,
				ServiceMetaData.class);
	}

}
