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

import java.io.IOException;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.web.as.WebServices;

/**
 * <pre>
 * Stage: PRE_DESCRIBE
 *    In: RackApplicationMetaData
 *   Out: RackApplicationMetaData, RackApplicationFactory
 * </pre>
 * 
 */
public class RackApplicationFactoryDeployer implements DeploymentUnitProcessor {

    public static final String SYNTHETIC_CONFIG_RU_NAME = "torquebox-synthetic-config.ru";

    private static final VirtualFileFilter RB_FILTER = new VirtualFileFilter() {
        @Override
        public boolean accepts(VirtualFile file) {
            return file.getName().endsWith( ".rb" );
        }
    };

    //private InjectionAnalyzer injectionAnalyzer;

    public RackApplicationFactoryDeployer() {
    }

    /*
    public void setInjectionAnalyzer(InjectionAnalyzer injectionAnalyzer) {
        this.injectionAnalyzer = injectionAnalyzer;
    }

    public InjectionAnalyzer getInjectionAnalyzer() {
        return this.injectionAnalyzer;
    }
    */


    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        String deploymentName = unit.getName();

        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rackAppMetaData == null) {
            return;
        }

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );

        RackApplicationFactoryImpl factory = new RackApplicationFactoryImpl();
        try {
            factory.setRackUpScript( rackAppMetaData.getRackUpScript( rubyAppMetaData.getRoot() ) );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }

        VirtualFile rackUpScriptLocation = rackAppMetaData.getRackUpScriptFile( rubyAppMetaData.getRoot() );
        
        if (rackUpScriptLocation == null) {
            rackUpScriptLocation = rubyAppMetaData.getRoot().getChild( SYNTHETIC_CONFIG_RU_NAME );
        }
        
        factory.setRackUpFile( rackUpScriptLocation );
        
        RackApplicationFactoryService service = new RackApplicationFactoryService( factory );
        
        ServiceName name = WebServices.rackApplicationFactoryName( deploymentName );
        ServiceBuilder<RackApplicationFactory> builder = phaseContext.getServiceTarget().addService( name, service );
        //builder.addDependency( WebServices.rackApplicationFactoryName( deploymentName ), RackApplicationFactory.class, service.getRackApplicationFactoryInjector() );
        builder.setInitialMode( Mode.ON_DEMAND );
        // setUpInjections(...)
        builder.install();
    }
    

    /*
    public void setUpInjections(DeploymentUnit unit, BeanMetaDataBuilder beanBuilder, VirtualFile rackRoot, VirtualFile rackUpScriptLocation) throws Exception {

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
        RubyRuntimeMetaData.Version rubyVersion = null;

        if (runtimeMetaData != null) {
            rubyVersion = runtimeMetaData.getVersion();
        }

        if (rubyVersion == null) {
            rubyVersion = RubyRuntimeMetaData.Version.V1_8;
        }

        List<Injectable> injectables = new ArrayList<Injectable>();

        injectables.addAll( this.injectionAnalyzer.analyze( rackUpScriptLocation, rubyVersion ) );

        for (VirtualFile child : rackRoot.getChildren( RB_FILTER )) {
            injectables.addAll( this.injectionAnalyzer.analyze( child, rubyVersion ) );
        }

        injectables.addAll( this.injectionAnalyzer.analyzeRecursively( rackRoot.getChild( "app/controllers/" ), rubyVersion ) );
        injectables.addAll( this.injectionAnalyzer.analyzeRecursively( rackRoot.getChild( "app/models/" ), rubyVersion ) );
        injectables.addAll( this.injectionAnalyzer.analyzeRecursively( rackRoot.getChild( "lib/" ), rubyVersion ) );

        BaseRubyProxyInjectionBuilder injectionBuilder = new BaseRubyProxyInjectionBuilder( unit, beanBuilder );
        injectionBuilder.injectInjectionRegistry( injectables );
    }
    */

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }
}
