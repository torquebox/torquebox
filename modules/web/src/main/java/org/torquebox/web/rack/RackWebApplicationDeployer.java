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

package org.torquebox.web.rack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.deployment.ServletContextAttribute;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.metadata.javaee.spec.EmptyMetaData;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.FilterMappingMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.jboss.metadata.web.spec.WebFragmentMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.torquebox.web.as.WebServices;
import org.torquebox.web.servlet.RackFilter;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: RackApplicationMetaData, WebMetaData, JBossWebMetaData
 *   Out: WebMetaData, JBossWebMetaData
 * </pre>
 * 
 * Makes the JBossWebMetaData depend on the RackApplicationPool, and sets up
 * Java servlet filters to delegate to the Rack application
 */
public class RackWebApplicationDeployer implements DeploymentUnitProcessor {

    public static final String RACK_FILTER_NAME = "torquebox.rack";

    public static final String STATIC_RESROUCE_SERVLET_NAME = "torquebox.static";
    public static final String STATIC_RESOURCE_SERVLET_CLASS_NAME = "org.torquebox.rack.core.servlet.StaticResourceServlet";

    public static final String FIVE_HUNDRED_SERVLET_NAME = "torquebox.500";
    public static final String FIVE_HUNDRED_SERVLET_CLASS_NAME = "org.torquebox.rack.core.servlet.FiveHundredServlet";

    public static final String LOCALHOST_MBEAN_NAME = "jboss.web:host=localhost,type=Host";

    public static final String EXPANDED_WAR_URL_ATTACHMENT_NAME = "org.jboss.web.expandedWarURL";

    public RackWebApplicationDeployer() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rackAppMetaData == null) {
            return;
        }

        WarMetaData warMetaData = unit.getAttachment( WarMetaData.ATTACHMENT_KEY );

        if (warMetaData == null) {
            warMetaData = new WarMetaData();
        }

        WebMetaData webMetaData = warMetaData.getWebMetaData();

        if (webMetaData == null) {
            webMetaData = new WebMetaData();
            webMetaData.setDistributable( new EmptyMetaData() );
            warMetaData.setWebMetaData( webMetaData );
        }

        Map<String, WebFragmentMetaData> webFragmentsMetaData = warMetaData.getWebFragmentsMetaData();

        if (webFragmentsMetaData == null) {
            webFragmentsMetaData = new HashMap<String, WebFragmentMetaData>();
            warMetaData.setWebFragmentsMetaData( webFragmentsMetaData );
        }

        JBossWebMetaData jbossWebMetaData = warMetaData.getJbossWebMetaData();
        
        if ( jbossWebMetaData == null ) {
            jbossWebMetaData = new JBossWebMetaData();
            warMetaData.setJbossWebMetaData( jbossWebMetaData );
        }

        setUpRackFilter( unit, rackAppMetaData, jbossWebMetaData );
        setUpStaticResourceServlet( rackAppMetaData, jbossWebMetaData );
        ensureSomeServlet( rackAppMetaData, jbossWebMetaData );
        try {
            setUpHostAndContext( unit, rackAppMetaData, warMetaData, jbossWebMetaData );
            setUpPoolDependency( rackAppMetaData, jbossWebMetaData );
        } catch (Exception e) {
            throw new DeploymentUnitProcessingException( e );
        }
        
        ServletContextAttribute serviceRegistryValue = new ServletContextAttribute("service.registry", unit.getServiceRegistry() );
        unit.addToAttachmentList( ServletContextAttribute.ATTACHMENT_KEY, serviceRegistryValue );
        
        unit.addToAttachmentList( Attachments.WEB_DEPENDENCIES, WebServices.rackApplicationPoolName( unit.getName() ) );

        /*
         * RubyApplicationMetaData rubyAppMetaData = unit.getAttachment(
         * RubyApplicationMetaData.ATTACHMENT_KEY ); try { URL docBaseUrl =
         * rubyAppMetaData.getRoot().toURL();
         * unit.addAttachment(EXPANDED_WAR_URL_ATTACHMENT_NAME, docBaseUrl,
         * URL.class); } catch (MalformedURLException e) { throw new
         * DeploymentException(e); }
         */
    }

    protected void setUpRackFilter(DeploymentUnit unit, RackApplicationMetaData rackAppMetaData, JBossWebMetaData jbossWebMetaData) {
        FilterMetaData rackFilter = new FilterMetaData();
        rackFilter.setId( RACK_FILTER_NAME );
        rackFilter.setFilterClass( RackFilter.class.getName() );
        rackFilter.setFilterName( RACK_FILTER_NAME );

        List<ParamValueMetaData> initParams = new ArrayList<ParamValueMetaData>();
        ParamValueMetaData rackAppFactory = new ParamValueMetaData();
        rackAppFactory.setParamName( RackFilter.RACK_APP_DEPLOYMENT_INIT_PARAM );
        rackAppFactory.setParamValue( unit.getName() );
        initParams.add( rackAppFactory );

        rackFilter.setInitParam( initParams );

        FiltersMetaData filters = jbossWebMetaData.getFilters();

        if (filters == null) {
            filters = new FiltersMetaData();
            jbossWebMetaData.setFilters( filters );
        }

        filters.add( rackFilter );

        FilterMappingMetaData filterMapping = new FilterMappingMetaData();
        filterMapping.setFilterName( RACK_FILTER_NAME );
        filterMapping.setUrlPatterns( Collections.singletonList( "*" ) );

        List<FilterMappingMetaData> filterMappings = jbossWebMetaData.getFilterMappings();

        if (filterMappings == null) {
            filterMappings = new ArrayList<FilterMappingMetaData>();
            jbossWebMetaData.setFilterMappings( filterMappings );
        }

        filterMappings.add( filterMapping );

    }

    protected void setUpStaticResourceServlet(RackApplicationMetaData rackAppMetaData, JBossWebMetaData jbossWebMetaData) {
        JBossServletsMetaData servlets = jbossWebMetaData.getServlets();
        if (servlets == null) {
            servlets = new JBossServletsMetaData();
            jbossWebMetaData.setServlets( servlets );
        }

        List<ServletMappingMetaData> servletMappings = jbossWebMetaData.getServletMappings();
        if (servletMappings == null) {
            servletMappings = new ArrayList<ServletMappingMetaData>();
            jbossWebMetaData.setServletMappings( servletMappings );
        }

        if (rackAppMetaData.getStaticPathPrefix() != null) {
            JBossServletMetaData staticServlet = new JBossServletMetaData();
            staticServlet.setServletClass( STATIC_RESOURCE_SERVLET_CLASS_NAME );
            staticServlet.setServletName( STATIC_RESROUCE_SERVLET_NAME );
            staticServlet.setId( STATIC_RESROUCE_SERVLET_NAME );

            ParamValueMetaData resourceRootParam = new ParamValueMetaData();
            resourceRootParam.setParamName( "resource.root" );
            resourceRootParam.setParamValue( rackAppMetaData.getStaticPathPrefix() );
            staticServlet.setInitParam( Collections.singletonList( resourceRootParam ) );
            servlets.add( staticServlet );

            ServletMappingMetaData staticMapping = new ServletMappingMetaData();
            staticMapping.setServletName( STATIC_RESROUCE_SERVLET_NAME );
            staticMapping.setUrlPatterns( Collections.singletonList( "/*" ) );

            servletMappings.add( staticMapping );
        }
    }

    protected void ensureSomeServlet(RackApplicationMetaData rackAppMetaData, JBossWebMetaData jbossWebMetaData) {
        JBossServletsMetaData servlets = jbossWebMetaData.getServlets();

        if (servlets.isEmpty()) {
            JBossServletMetaData fiveHundredServlet = new JBossServletMetaData();
            fiveHundredServlet.setServletClass( FIVE_HUNDRED_SERVLET_CLASS_NAME );
            fiveHundredServlet.setServletName( FIVE_HUNDRED_SERVLET_NAME );
            fiveHundredServlet.setId( FIVE_HUNDRED_SERVLET_NAME );
            servlets.add( fiveHundredServlet );

            ServletMappingMetaData fiveHundredMapping = new ServletMappingMetaData();
            fiveHundredMapping.setServletName( FIVE_HUNDRED_SERVLET_NAME );
            fiveHundredMapping.setUrlPatterns( Collections.singletonList( "/*" ) );

            List<ServletMappingMetaData> servletMappings = jbossWebMetaData.getServletMappings();
            servletMappings.add( fiveHundredMapping );
        }
    }

    protected void setUpHostAndContext(DeploymentUnit unit, RackApplicationMetaData rackAppMetaData, WarMetaData warMetaData, JBossWebMetaData jbossWebMetaData)
            throws Exception {

        /*
        if (jbossWebMetaData.getDistributable() != null) {
            jbossWebMetaData.setDistributable( jbossWebMetaData.getDistributable() );
            ReplicationConfig repCfg = new ReplicationConfig();
            repCfg.setReplicationGranularity( ReplicationGranularity.SESSION );
            repCfg.setReplicationTrigger( ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET );
            jbossWebMetaData.setReplicationConfig( repCfg );
        }
        */

        jbossWebMetaData.setContextRoot( rackAppMetaData.getContextPath() );
    }

    protected void setUpPoolDependency(RackApplicationMetaData rackAppMetaData, JBossWebMetaData jbossWebMetaData) {
        List<String> depends = jbossWebMetaData.getDepends();

        if (depends == null) {
            depends = new ArrayList<String>();
            jbossWebMetaData.setDepends( depends );
        }

        depends.add( rackAppMetaData.getRackApplicationPoolName() );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

}
