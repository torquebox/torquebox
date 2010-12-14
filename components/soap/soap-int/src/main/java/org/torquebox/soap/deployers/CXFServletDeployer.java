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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cxf.common.logging.LogUtils;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.torquebox.soap.core.cxf.RubyCXFServlet;
import org.torquebox.soap.core.cxf.util.JBossLoggerBridge;
import org.torquebox.soap.metadata.SOAPServiceMetaData;

public class CXFServletDeployer extends AbstractDeployer {
	static {
		LogUtils.setLoggerClass( JBossLoggerBridge.class );
	}
	
	public static final String SERVLET_NAME = "ruby-cxf-servlet";
	
	public CXFServletDeployer() {
		addInput( SOAPServiceMetaData.class );
		addInput( JBossWebMetaData.class );
		addOutput( JBossWebMetaData.class );
		setStage( DeploymentStages.PRE_REAL );
		setRelativeOrder( 2000 );
	}
	
	public void deploy(DeploymentUnit unit) throws DeploymentException {
	    
	    if ( unit.getAllMetaData( SOAPServiceMetaData.class).isEmpty() ) {
	        return;
	    }
		
		log.debug( "Deploy CXF servlet for " + unit );
		JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class );
		
		if ( webMetaData == null ) {
			webMetaData = new JBossWebMetaData();
			unit.addAttachment( JBossWebMetaData.class, webMetaData );
			log.debug( "attaching " + webMetaData );
		}
		
		JBossServletMetaData servletMetaData = new JBossServletMetaData();
		servletMetaData.setId( SERVLET_NAME );
		servletMetaData.setServletName( SERVLET_NAME );
		servletMetaData.setServletClass( RubyCXFServlet.class.getName() );
		servletMetaData.setLoadOnStartupInt( 1 );
		
		List<ParamValueMetaData> initParams = new ArrayList<ParamValueMetaData>();
		ParamValueMetaData cxfBusNameParam = new ParamValueMetaData();
		cxfBusNameParam.setParamName( "cxf.bus.name" );
		String busName =  CXFBusDeployer.getBusName( unit.getSimpleName() );
		cxfBusNameParam.setParamValue( busName );
		initParams.add( cxfBusNameParam );
		servletMetaData.setInitParam( initParams );
		
		JBossServletsMetaData servlets = webMetaData.getServlets();
		if ( servlets == null ) {
			servlets = new JBossServletsMetaData();
			webMetaData.setServlets( servlets );
		}
		
		servlets.add( servletMetaData );
		
		ServletMappingMetaData servletMapping = new ServletMappingMetaData();
		servletMapping.setServletName( SERVLET_NAME );
		servletMapping.setUrlPatterns( Collections.singletonList( "/endpoints/*" ) );
		
		List<ServletMappingMetaData> servletMappings = webMetaData.getServletMappings();
		
		if ( servletMappings == null ) {
			servletMappings = new ArrayList<ServletMappingMetaData>();
			webMetaData.setServletMappings(servletMappings);
		}
		
		servletMappings.add( servletMapping );
	}

}
