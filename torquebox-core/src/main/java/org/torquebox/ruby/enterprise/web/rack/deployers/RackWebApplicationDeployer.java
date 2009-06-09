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
package org.torquebox.ruby.enterprise.web.rack.deployers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.FilterMappingMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.torquebox.ruby.enterprise.web.StaticResourceServlet;
import org.torquebox.ruby.enterprise.web.rack.RackFilter;
import org.torquebox.ruby.enterprise.web.rack.metadata.RackWebApplicationMetaData;

public class RackWebApplicationDeployer extends AbstractSimpleVFSRealDeployer<RackWebApplicationMetaData> {

	public static final String FILTER_NAME = "torquebox.rack";
	public static final String SERVLET_NAME = "torquebox.static";
	
	private static final Logger log = Logger.getLogger(RackWebApplicationDeployer.class);

	public RackWebApplicationDeployer() {
		super(RackWebApplicationMetaData.class);
		addInput(JBossWebMetaData.class);
		addOutput(JBossWebMetaData.class);
		setStage(DeploymentStages.PRE_REAL);
		setRelativeOrder(1000);
	}

	@Override
	public void deploy(VFSDeploymentUnit unit, RackWebApplicationMetaData metaData) throws DeploymentException {

		log.debug("deploying " + unit);

		JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);

		if (webMetaData == null) {
			webMetaData = new JBossWebMetaData();
			unit.addAttachment(JBossWebMetaData.class, webMetaData);
		}

		FilterMetaData rackFilter = new FilterMetaData();
		rackFilter.setId( FILTER_NAME );
		rackFilter.setFilterClass(RackFilter.class.getName());
		rackFilter.setFilterName( FILTER_NAME );

		List<ParamValueMetaData> initParams = new ArrayList<ParamValueMetaData>();
		ParamValueMetaData rackAppFactory = new ParamValueMetaData();
		rackAppFactory.setParamName(RackFilter.RACK_APP_POOL_INIT_PARAM);
		rackAppFactory.setParamValue(metaData.getRackApplicationFactoryName() + ".pool");
		initParams.add(rackAppFactory);

		rackFilter.setInitParam(initParams);

		FiltersMetaData filters = new FiltersMetaData();
		filters.add(rackFilter);

		webMetaData.setFilters(filters);

		FilterMappingMetaData filterMapping = new FilterMappingMetaData();
		filterMapping.setFilterName( FILTER_NAME );
		filterMapping.setUrlPatterns(Collections.singletonList("/*"));

		List<FilterMappingMetaData> filterMappings = webMetaData.getFilterMappings();

		if (filterMappings == null) {
			filterMappings = new ArrayList<FilterMappingMetaData>();
			webMetaData.setFilterMappings(filterMappings);
		}

		filterMappings.add(filterMapping);

		webMetaData.setContextRoot(metaData.getContext());
		if (metaData.getHost() != null) {
			webMetaData.setVirtualHosts(Collections.singletonList(metaData.getHost()));
		}

		if (metaData.getStaticPathPrefix() != null) {
			JBossServletsMetaData servlets = new JBossServletsMetaData();
			JBossServletMetaData staticServlet = new JBossServletMetaData();
			staticServlet.setServletClass(StaticResourceServlet.class.getName());
			staticServlet.setServletName( SERVLET_NAME );
			staticServlet.setId( SERVLET_NAME );

			ParamValueMetaData resourceRootParam = new ParamValueMetaData();
			resourceRootParam.setParamName("resource.root");
			resourceRootParam.setParamValue(metaData.getStaticPathPrefix());
			staticServlet.setInitParam(Collections.singletonList(resourceRootParam));
			servlets.add(staticServlet);
			webMetaData.setServlets(servlets);

			ServletMappingMetaData staticMapping = new ServletMappingMetaData();
			staticMapping.setServletName( SERVLET_NAME );
			staticMapping.setUrlPatterns(Collections.singletonList("/*"));

			List<ServletMappingMetaData> servletMappings = webMetaData.getServletMappings();
			if (servletMappings == null) {
				servletMappings = new ArrayList<ServletMappingMetaData>();
				webMetaData.setServletMappings(servletMappings);
			}
			servletMappings.add(staticMapping);
		}

		unit.addAttachment(JBossWebMetaData.class, webMetaData);
	}
}
