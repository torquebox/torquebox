/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.soap.deployers;

import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.soap.core.RubySOAPService;
import org.torquebox.soap.metadata.SOAPServiceMetaData;

/**
 * REAL stage deployer to deploy all <code>RubyEndpointMetaData</code> in a
 * unit.
 * 
 * This deployer will seek out all instances of RubyWebServiceMetaData attached
 * to the unit, regardless of name, and deploy them.
 * 
 * Output is zero-or-more <code>BeanMetaData</code> describing
 * <code>RubyWebService</code> instances.
 * 
 * @author Bob McWhirter
 */
public class SOAPServicesDeployer extends AbstractDeployer {

	private static final String BEAN_PREFIX = "jboss.ruby.enterprise.webservices";

	private static final Logger log = Logger.getLogger(SOAPServicesDeployer.class);

	public SOAPServicesDeployer() {
	    setStage(DeploymentStages.REAL);
	    addInput(SOAPServiceMetaData.class);
		setOutput(BeanMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
	    
	    Set<? extends SOAPServiceMetaData> allMetaData = unit.getAllMetaData( SOAPServiceMetaData.class );
	    
	    if ( allMetaData.isEmpty() ) {
	        return;
	    }
	    
		BeanMetaData busBean = unit.getAttachment(BeanMetaData.class + "$cxf.bus", BeanMetaData.class);
		
		if (busBean == null) {
			throw new DeploymentException("No CXF Bus available");
		}
		
	    for ( SOAPServiceMetaData each : allMetaData ) {
			deployWebService( unit, busBean, each );
	    }
	}

	public void deployWebService(DeploymentUnit unit, BeanMetaData busBean, SOAPServiceMetaData metaData) throws DeploymentException {
		log.debug("Deploying webservices for : " + metaData);

		String beanName = BEAN_PREFIX + "." + unit.getSimpleName() + "." + metaData.getName();

		BeanMetaDataBuilder beanBuilder = BeanMetaDataBuilder.createBuilder(beanName, RubySOAPService.class.getName());

		String runtimePoolName = AttachmentUtils.beanName( unit, RubyRuntimePool.class, "soap" );
		
		ValueMetaData poolInjection = beanBuilder.createInject(runtimePoolName);
		beanBuilder.addPropertyMetaData("rubyRuntimePool", poolInjection);
		beanBuilder.addPropertyMetaData("name", metaData.getName());
		beanBuilder.addPropertyMetaData("classLocation", metaData.getClassLocation());
		beanBuilder.addPropertyMetaData("rubyClassName", metaData.getEndpointClassName());
		beanBuilder.addPropertyMetaData("wsdlLocation", metaData.getWsdlLocation());
		beanBuilder.addPropertyMetaData("targetNamespace", metaData.getTargetNamespace());
		beanBuilder.addPropertyMetaData("portName", metaData.getPortName());
		beanBuilder.addPropertyMetaData("address", "/" + metaData.getName());

		ValueMetaData typeSpaceInjection = beanBuilder.createInject(RubyTypeSpaceDeployer.getBeanName(unit, metaData.getName()));
		beanBuilder.addPropertyMetaData("rubyTypeSpace", typeSpaceInjection);

		ValueMetaData busInjection = beanBuilder.createInject(busBean.getName());
		beanBuilder.addPropertyMetaData("bus", busInjection);

		BeanMetaData beanMetaData = beanBuilder.getBeanMetaData();
		unit.addAttachment(BeanMetaData.class + "$endpoint." + metaData.getName(), beanMetaData, BeanMetaData.class);
	}

}
