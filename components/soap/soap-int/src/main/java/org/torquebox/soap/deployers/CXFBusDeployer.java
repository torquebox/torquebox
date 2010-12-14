/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.soap.core.cxf.RubyCXFBus;
import org.torquebox.soap.metadata.SOAPServiceMetaData;

/** REAL deployer to provision a CXF bus if a SOAPServiceMetaData is present.
 * 
 * @author Bob McWhirter
 */
public class CXFBusDeployer extends AbstractDeployer {
	
	public static final String PREFIX = "torquebox.cxf.bus";
	
	public CXFBusDeployer() {
		addInput(SOAPServiceMetaData.class);
		addOutput( BeanMetaData.class );
		setStage( DeploymentStages.POST_CLASSLOADER );
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
	    
	    if ( unit.getAllMetaData( SOAPServiceMetaData.class).isEmpty() ) {
	        return;
	    }
		
		String beanName = getBusName( unit.getSimpleName() );
		BeanMetaDataBuilder beanBuilder = BeanMetaDataBuilder.createBuilder( beanName, RubyCXFBus.class.getName() );
		BeanMetaData beanMetaData = beanBuilder.getBeanMetaData();
		unit.addAttachment( BeanMetaData.class + "$cxf.bus", beanMetaData );
	}
	
	public static String getBusName(String simpleName) {
		return PREFIX + "." + simpleName;
	}

}
