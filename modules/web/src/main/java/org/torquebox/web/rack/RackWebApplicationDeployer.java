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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.web.SharedTldsMetaDataBuilder;
import org.jboss.as.web.deployment.ServletContextAttribute;
import org.jboss.as.web.deployment.TldsMetaData;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EmptyMetaData;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.FilterMappingMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.metadata.web.spec.MimeMappingMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.jboss.metadata.web.spec.WebFragmentMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;
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
    public static final String STATIC_RESOURCE_SERVLET_CLASS_NAME = "org.torquebox.web.servlet.StaticResourceServlet";

    public static final String FIVE_HUNDRED_SERVLET_NAME = "torquebox.500";
    public static final String FIVE_HUNDRED_SERVLET_CLASS_NAME = "org.torquebox.web.servlet.FiveHundredServlet";

    public static final String LOCALHOST_MBEAN_NAME = "jboss.web:host=localhost,type=Host";

    public static final String EXPANDED_WAR_URL_ATTACHMENT_NAME = "org.jboss.web.expandedWarURL";

    public RackWebApplicationDeployer() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );

        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rackAppMetaData == null) {
            return;
        }

        log.info( "Marking as WAR" );
        DeploymentTypeMarker.setType( DeploymentType.WAR, unit );
        WarMetaData warMetaData = new WarMetaData();
        
        final TldsMetaData tldsMetaData = new TldsMetaData();
        // HACK: Remove reflection once SharedTldsMetaDataBuilder's constructor is public
        try {
            Constructor<SharedTldsMetaDataBuilder> ctor = SharedTldsMetaDataBuilder.class.getDeclaredConstructor( ModelNode.class );
            ctor.setAccessible( true );
            tldsMetaData.setSharedTlds( ctor.newInstance( new Object[] { null } ) );
        } catch (Exception e) {
            throw new DeploymentUnitProcessingException( e );
        }
        unit.putAttachment( TldsMetaData.ATTACHMENT_KEY, tldsMetaData );
        unit.putAttachment( WarMetaData.ATTACHMENT_KEY, warMetaData );
        unit.addToAttachmentList( Attachments.RESOURCE_ROOTS, resourceRoot );

        WebMetaData webMetaData = warMetaData.getWebMetaData();

        if (webMetaData == null) {
            webMetaData = new WebMetaData();
            ServiceName jgroups = ChannelFactoryService.getServiceName(null);
            if (phaseContext.getServiceRegistry().getService( jgroups ) != null) {
                log.info("Marking app as distributable");
                webMetaData.setDistributable( new EmptyMetaData() );
            }
            warMetaData.setWebMetaData( webMetaData );
        }

        Map<String, WebFragmentMetaData> webFragmentsMetaData = warMetaData.getWebFragmentsMetaData();

        if (webFragmentsMetaData == null) {
            webFragmentsMetaData = new HashMap<String, WebFragmentMetaData>();
            warMetaData.setWebFragmentsMetaData( webFragmentsMetaData );
        }

        JBossWebMetaData jbossWebMetaData = warMetaData.getJbossWebMetaData();

        if (jbossWebMetaData == null) {
            jbossWebMetaData = new JBossWebMetaData();
            warMetaData.setJbossWebMetaData( jbossWebMetaData );
        }

        setUpMimeTypes( jbossWebMetaData );
        setUpRackFilter( unit, rackAppMetaData, jbossWebMetaData );
        setUpStaticResourceServlet( rackAppMetaData, jbossWebMetaData );
        ensureSomeServlet( rackAppMetaData, jbossWebMetaData );
        try {
            setUpHostAndContext( unit, rackAppMetaData, warMetaData, jbossWebMetaData );
            setUpPoolDependency( rackAppMetaData, jbossWebMetaData );
        } catch (Exception e) {
            throw new DeploymentUnitProcessingException( e );
        }

        jbossWebMetaData.setVirtualHosts( rackAppMetaData.getHosts() );

        ServletContextAttribute serviceRegistryValue = new ServletContextAttribute( "service.registry", unit.getServiceRegistry() );
        unit.addToAttachmentList( ServletContextAttribute.ATTACHMENT_KEY, serviceRegistryValue );

        ServletContextAttribute componentResolverNameValue = new ServletContextAttribute( "component.resolver.service-name",
                WebServices.rackApplicationComponentResolver( unit ) );
        unit.addToAttachmentList( ServletContextAttribute.ATTACHMENT_KEY, componentResolverNameValue );
        unit.addToAttachmentList( Attachments.WEB_DEPENDENCIES, WebServices.rackApplicationComponentResolver( unit ) );

        ServletContextAttribute runtimePoolNameValue = new ServletContextAttribute( "runtime.pool.service-name", CoreServices.runtimePoolName( unit, "web" ));
        unit.addToAttachmentList( ServletContextAttribute.ATTACHMENT_KEY, runtimePoolNameValue );
        unit.addToAttachmentList( Attachments.WEB_DEPENDENCIES, CoreServices.runtimePoolName( unit, "web" ) );
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
    
    protected MimeMappingMetaData createMimeMapping(String extension, String mimeType) {
        MimeMappingMetaData mapping = new MimeMappingMetaData();
        mapping.setExtension(extension);
        mapping.setMimeType(mimeType);
        return mapping;
    }
    
    // TODO: Mark our deployment as a .war early enough so SharedWebMetaDataBuilder can add these
    // mime types instead of having to copy them here
    protected void setUpMimeTypes(JBossWebMetaData jbossWebMetaData) {
        List<MimeMappingMetaData> mappings = new ArrayList<MimeMappingMetaData>();
        mappings.add(createMimeMapping("abs", "audio/x-mpeg"));
        mappings.add(createMimeMapping("ai", "application/postscript"));
        mappings.add(createMimeMapping("aif", "audio/x-aiff"));
        mappings.add(createMimeMapping("aifc", "audio/x-aiff"));
        mappings.add(createMimeMapping("aiff", "audio/x-aiff"));
        mappings.add(createMimeMapping("aim", "application/x-aim"));
        mappings.add(createMimeMapping("art", "image/x-jg"));
        mappings.add(createMimeMapping("asf", "video/x-ms-asf"));
        mappings.add(createMimeMapping("asx", "video/x-ms-asf"));
        mappings.add(createMimeMapping("au", "audio/basic"));
        mappings.add(createMimeMapping("avi", "video/x-msvideo"));
        mappings.add(createMimeMapping("avx", "video/x-rad-screenplay"));
        mappings.add(createMimeMapping("bcpio", "application/x-bcpio"));
        mappings.add(createMimeMapping("bin", "application/octet-stream"));
        mappings.add(createMimeMapping("bmp", "image/bmp"));
        mappings.add(createMimeMapping("body", "text/html"));
        mappings.add(createMimeMapping("cdf", "application/x-cdf"));
        mappings.add(createMimeMapping("cer", "application/x-x509-ca-cert"));
        mappings.add(createMimeMapping("class", "application/java"));
        mappings.add(createMimeMapping("cpio", "application/x-cpio"));
        mappings.add(createMimeMapping("csh", "application/x-csh"));
        mappings.add(createMimeMapping("css", "text/css"));
        mappings.add(createMimeMapping("dib", "image/bmp"));
        mappings.add(createMimeMapping("doc", "application/msword"));
        mappings.add(createMimeMapping("dtd", "application/xml-dtd"));
        mappings.add(createMimeMapping("dv", "video/x-dv"));
        mappings.add(createMimeMapping("dvi", "application/x-dvi"));
        mappings.add(createMimeMapping("eps", "application/postscript"));
        mappings.add(createMimeMapping("etx", "text/x-setext"));
        mappings.add(createMimeMapping("exe", "application/octet-stream"));
        mappings.add(createMimeMapping("gif", "image/gif"));
        mappings.add(createMimeMapping("gtar", "application/x-gtar"));
        mappings.add(createMimeMapping("gz", "application/x-gzip"));
        mappings.add(createMimeMapping("hdf", "application/x-hdf"));
        mappings.add(createMimeMapping("hqx", "application/mac-binhex40"));
        mappings.add(createMimeMapping("htc", "text/x-component"));
        mappings.add(createMimeMapping("htm", "text/html"));
        mappings.add(createMimeMapping("html", "text/html"));
        mappings.add(createMimeMapping("hqx", "application/mac-binhex40"));
        mappings.add(createMimeMapping("ief", "image/ief"));
        mappings.add(createMimeMapping("jad", "text/vnd.sun.j2me.app-descriptor"));
        mappings.add(createMimeMapping("jar", "application/java-archive"));
        mappings.add(createMimeMapping("java", "text/plain"));
        mappings.add(createMimeMapping("jnlp", "application/x-java-jnlp-file"));
        mappings.add(createMimeMapping("jpe", "image/jpeg"));
        mappings.add(createMimeMapping("jpeg", "image/jpeg"));
        mappings.add(createMimeMapping("jpg", "image/jpeg"));
        mappings.add(createMimeMapping("js", "text/javascript"));
        mappings.add(createMimeMapping("jsf", "text/plain"));
        mappings.add(createMimeMapping("jspf", "text/plain"));
        mappings.add(createMimeMapping("kar", "audio/x-midi"));
        mappings.add(createMimeMapping("latex", "application/x-latex"));
        mappings.add(createMimeMapping("m3u", "audio/x-mpegurl"));
        mappings.add(createMimeMapping("mac", "image/x-macpaint"));
        mappings.add(createMimeMapping("man", "application/x-troff-man"));
        mappings.add(createMimeMapping("mathml", "application/mathml+xml"));
        mappings.add(createMimeMapping("me", "application/x-troff-me"));
        mappings.add(createMimeMapping("mid", "audio/x-midi"));
        mappings.add(createMimeMapping("midi", "audio/x-midi"));
        mappings.add(createMimeMapping("mif", "application/x-mif"));
        mappings.add(createMimeMapping("mov", "video/quicktime"));
        mappings.add(createMimeMapping("movie", "video/x-sgi-movie"));
        mappings.add(createMimeMapping("mp1", "audio/x-mpeg"));
        mappings.add(createMimeMapping("mp2", "audio/x-mpeg"));
        mappings.add(createMimeMapping("mp3", "audio/x-mpeg"));
        mappings.add(createMimeMapping("mp4", "video/mp4"));
        mappings.add(createMimeMapping("mpa", "audio/x-mpeg"));
        mappings.add(createMimeMapping("mpe", "video/mpeg"));
        mappings.add(createMimeMapping("mpeg", "video/mpeg"));
        mappings.add(createMimeMapping("mpega", "audio/x-mpeg"));
        mappings.add(createMimeMapping("mpg", "video/mpeg"));
        mappings.add(createMimeMapping("mpv2", "video/mpeg2"));
        mappings.add(createMimeMapping("ms", "application/x-wais-source"));
        mappings.add(createMimeMapping("nc", "application/x-netcdf"));
        mappings.add(createMimeMapping("oda", "application/oda"));
        mappings.add(createMimeMapping("odb", "application/vnd.oasis.opendocument.database"));
        mappings.add(createMimeMapping("odc", "application/vnd.oasis.opendocument.chart"));
        mappings.add(createMimeMapping("odf", "application/vnd.oasis.opendocument.formula"));
        mappings.add(createMimeMapping("odg", "application/vnd.oasis.opendocument.graphics"));
        mappings.add(createMimeMapping("odi", "application/vnd.oasis.opendocument.image"));
        mappings.add(createMimeMapping("odm", "application/vnd.oasis.opendocument.text-master"));
        mappings.add(createMimeMapping("odp", "application/vnd.oasis.opendocument.presentation"));
        mappings.add(createMimeMapping("ods", "application/vnd.oasis.opendocument.spreadsheet"));
        mappings.add(createMimeMapping("odt", "application/vnd.oasis.opendocument.text"));
        mappings.add(createMimeMapping("otg ", "application/vnd.oasis.opendocument.graphics-template"));
        mappings.add(createMimeMapping("oth", "application/vnd.oasis.opendocument.text-web"));
        mappings.add(createMimeMapping("otp", "application/vnd.oasis.opendocument.presentation-template"));
        mappings.add(createMimeMapping("ots", "application/vnd.oasis.opendocument.spreadsheet-template "));
        mappings.add(createMimeMapping("ott", "application/vnd.oasis.opendocument.text-template"));
        mappings.add(createMimeMapping("ogx", "application/ogg"));
        mappings.add(createMimeMapping("ogv", "video/ogg"));
        mappings.add(createMimeMapping("oga", "audio/ogg"));
        mappings.add(createMimeMapping("ogg", "audio/ogg"));
        mappings.add(createMimeMapping("spx", "audio/ogg"));
        mappings.add(createMimeMapping("flac", "audio/flac"));
        mappings.add(createMimeMapping("anx", "application/annodex"));
        mappings.add(createMimeMapping("axa", "audio/annodex"));
        mappings.add(createMimeMapping("axv", "video/annodex"));
        mappings.add(createMimeMapping("xspf", "application/xspf+xml"));
        mappings.add(createMimeMapping("pbm", "image/x-portable-bitmap"));
        mappings.add(createMimeMapping("pct", "image/pict"));
        mappings.add(createMimeMapping("pdf", "application/pdf"));
        mappings.add(createMimeMapping("pgm", "image/x-portable-graymap"));
        mappings.add(createMimeMapping("pic", "image/pict"));
        mappings.add(createMimeMapping("pict", "image/pict"));
        mappings.add(createMimeMapping("pls", "audio/x-scpls"));
        mappings.add(createMimeMapping("png", "image/png"));
        mappings.add(createMimeMapping("pnm", "image/x-portable-anymap"));
        mappings.add(createMimeMapping("pnt", "image/x-macpaint"));
        mappings.add(createMimeMapping("ppm", "image/x-portable-pixmap"));
        mappings.add(createMimeMapping("ppt", "application/powerpoint"));
        mappings.add(createMimeMapping("ps", "application/postscript"));
        mappings.add(createMimeMapping("psd", "image/x-photoshop"));
        mappings.add(createMimeMapping("qt", "video/quicktime"));
        mappings.add(createMimeMapping("qti", "image/x-quicktime"));
        mappings.add(createMimeMapping("qtif", "image/x-quicktime"));
        mappings.add(createMimeMapping("ras", "image/x-cmu-raster"));
        mappings.add(createMimeMapping("rdf", "application/rdf+xml"));
        mappings.add(createMimeMapping("rgb", "image/x-rgb"));
        mappings.add(createMimeMapping("rm", "application/vnd.rn-realmedia"));
        mappings.add(createMimeMapping("roff", "application/x-troff"));
        mappings.add(createMimeMapping("rtf", "application/rtf"));
        mappings.add(createMimeMapping("rtx", "text/richtext"));
        mappings.add(createMimeMapping("sh", "application/x-sh"));
        mappings.add(createMimeMapping("shar", "application/x-shar"));
        mappings.add(createMimeMapping("smf", "audio/x-midi"));
        mappings.add(createMimeMapping("sit", "application/x-stuffit"));
        mappings.add(createMimeMapping("snd", "audio/basic"));
        mappings.add(createMimeMapping("src", "application/x-wais-source"));
        mappings.add(createMimeMapping("sv4cpio", "application/x-sv4cpio"));
        mappings.add(createMimeMapping("sv4crc", "application/x-sv4crc"));
        mappings.add(createMimeMapping("swf", "application/x-shockwave-flash"));
        mappings.add(createMimeMapping("t", "application/x-troff"));
        mappings.add(createMimeMapping("tar", "application/x-tar"));
        mappings.add(createMimeMapping("tcl", "application/x-tcl"));
        mappings.add(createMimeMapping("tex", "application/x-tex"));
        mappings.add(createMimeMapping("texi", "application/x-texinfo"));
        mappings.add(createMimeMapping("texinfo", "application/x-texinfo"));
        mappings.add(createMimeMapping("tif", "image/tiff"));
        mappings.add(createMimeMapping("tiff", "image/tiff"));
        mappings.add(createMimeMapping("tr", "application/x-troff"));
        mappings.add(createMimeMapping("tsv", "text/tab-separated-values"));
        mappings.add(createMimeMapping("txt", "text/plain"));
        mappings.add(createMimeMapping("ulw", "audio/basic"));
        mappings.add(createMimeMapping("ustar", "application/x-ustar"));
        mappings.add(createMimeMapping("vxml", "application/voicexml+xml"));
        mappings.add(createMimeMapping("xbm", "image/x-xbitmap"));
        mappings.add(createMimeMapping("xht", "application/xhtml+xml"));
        mappings.add(createMimeMapping("xhtml", "application/xhtml+xml"));
        mappings.add(createMimeMapping("xml", "application/xml"));
        mappings.add(createMimeMapping("xpm", "image/x-xpixmap"));
        mappings.add(createMimeMapping("xsl", "application/xml"));
        mappings.add(createMimeMapping("xslt", "application/xslt+xml"));
        mappings.add(createMimeMapping("xul", "application/vnd.mozilla.xul+xml"));
        mappings.add(createMimeMapping("xwd", "image/x-xwindowdump"));
        mappings.add(createMimeMapping("wav", "audio/x-wav"));
        mappings.add(createMimeMapping("svg", "image/svg+xml"));
        mappings.add(createMimeMapping("svgz", "image/svg+xml"));
        mappings.add(createMimeMapping("vsd", "application/x-visio"));
        mappings.add(createMimeMapping("wbmp", "image/vnd.wap.wbmp"));
        mappings.add(createMimeMapping("wml", "text/vnd.wap.wml"));
        mappings.add(createMimeMapping("wmlc", "application/vnd.wap.wmlc"));
        mappings.add(createMimeMapping("wmls", "text/vnd.wap.wmlscript"));
        mappings.add(createMimeMapping("wmlscriptc", "application/vnd.wap.wmlscriptc"));
        mappings.add(createMimeMapping("wmv", "video/x-ms-wmv"));
        mappings.add(createMimeMapping("wrl", "x-world/x-vrml"));
        mappings.add(createMimeMapping("wsdl", "text/xml"));
        mappings.add(createMimeMapping("xsd", "text/xml"));
        mappings.add(createMimeMapping("Z", "application/x-compress"));
        mappings.add(createMimeMapping("z", "application/x-compress"));
        mappings.add(createMimeMapping("zip", "application/zip"));
        mappings.add(createMimeMapping("xls", "application/vnd.ms-excel"));
        mappings.add(createMimeMapping("doc", "application/vnd.ms-word"));
        mappings.add(createMimeMapping("ppt", "application/vnd.ms-powerpoint"));
        
        jbossWebMetaData.setMimeMappings( mappings );
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack" );

}
