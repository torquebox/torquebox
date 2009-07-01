/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.torquebox.ruby.enterprise.sip.deployers;

import java.util.Iterator;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.ConvergedSipAnnotationMetaDataDeployer;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.sip.spec.ServletSelectionMetaData;
import org.jboss.metadata.sip.spec.Sip11MetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.torquebox.ruby.enterprise.sip.StandardSipRubyController;
import org.torquebox.ruby.enterprise.sip.metadata.SipApplicationMetaData;
import org.torquebox.ruby.enterprise.sip.metadata.SipRubyControllerMetaData;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class ConvergedSipRackWebApplicationDeployer extends AbstractSimpleVFSRealDeployer<SipApplicationMetaData> {

	private static final Logger log = Logger.getLogger(ConvergedSipRackWebApplicationDeployer.class);
	
	private Kernel kernel;
	/**
	 * 
	 */
	public ConvergedSipRackWebApplicationDeployer() {
		super(SipApplicationMetaData.class);
		addInput(JBossConvergedSipMetaData.class);
		addInput(SipRubyControllerMetaData.class);
		addOutput(JBossConvergedSipMetaData.class);
		setStage(DeploymentStages.PRE_REAL);
		setRelativeOrder(1001);
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, SipApplicationMetaData metaData)
			throws DeploymentException {
		log.debug("deploying " + unit);
		
		WebMetaData webMetaData = unit.getAttachment(WebMetaData.class);
		JBossWebMetaData jbossWebMetaData = unit.getAttachment(JBossWebMetaData.class);
		
		JBossConvergedSipMetaData convergedMetaData = unit.getAttachment(JBossConvergedSipMetaData.class);
		if (convergedMetaData == null) {
			convergedMetaData = new JBossConvergedSipMetaData();
			unit.addAttachment(JBossConvergedSipMetaData.class, convergedMetaData);
		}
		String sipKey = ConvergedSipAnnotationMetaDataDeployer.SIP_ANNOTATED_ATTACHMENT_NAME;
	    Sip11MetaData sipAnnotatedMetaData = unit.getAttachment(sipKey, Sip11MetaData.class);
	       
		convergedMetaData.merge(jbossWebMetaData, webMetaData);
		convergedMetaData.merge(sipAnnotatedMetaData, null);
		 
		convergedMetaData.setApplicationName(unit.getSimpleName());
		
		if(metaData.getRubyController() != null) {			
			ServletSelectionMetaData servletSelectionMetaData = new ServletSelectionMetaData();
						
			String rackAppFactoryName = null;
			FiltersMetaData filterDefs = convergedMetaData.getFilters();
			Iterator<FilterMetaData> it = filterDefs.iterator();
			while(it.hasNext() && rackAppFactoryName == null) {
				FilterMetaData filterDef = it.next();
				rackAppFactoryName = ((ParamValueMetaData)filterDef.getInitParam().get(0)).getParamValue();
			}						
						
			StandardSipRubyController sipRubyController = new StandardSipRubyController(
					metaData.getRubyController(), rackAppFactoryName, kernel);
			servletSelectionMetaData.setSipRubyController(sipRubyController);
			convergedMetaData.setServletSelection(servletSelectionMetaData);
		}
		
		unit.addAttachment(JBossConvergedSipMetaData.class, convergedMetaData);
		
	}

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

	public Kernel getKernel() {
		return kernel;
	}
	
	

}
