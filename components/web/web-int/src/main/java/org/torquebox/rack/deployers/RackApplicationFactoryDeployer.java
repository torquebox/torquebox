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

package org.torquebox.rack.deployers;

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.injection.BaseRubyProxyInjectionBuilder;
import org.torquebox.injection.Injectable;
import org.torquebox.injection.InjectionAnalyzer;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.RackApplicationFactoryImpl;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.spi.RackApplicationFactory;

/**
 * <pre>
 * Stage: PRE_DESCRIBE
 *    In: RackApplicationMetaData
 *   Out: RackApplicationMetaData, RackApplicationFactory
 * </pre>
 * 
 */
public class RackApplicationFactoryDeployer extends AbstractSimpleVFSRealDeployer<RackApplicationMetaData> {

    public static final String SYNTHETIC_CONFIG_RU_NAME = "torquebox-synthetic-config.ru";

    private static final VirtualFileFilter RB_FILTER = new VirtualFileFilter() {
        @Override
        public boolean accepts(VirtualFile file) {
            return file.getName().endsWith( ".rb" );
        }
    };

    private InjectionAnalyzer injectionAnalyzer;

    public RackApplicationFactoryDeployer() {
        super( RackApplicationMetaData.class );
        addRequiredInput( RubyApplicationMetaData.class );
        addOutput( RackApplicationMetaData.class );
        addOutput( BeanMetaData.class );
        setStage( DeploymentStages.PRE_DESCRIBE );
        setRelativeOrder( 500 );
    }

    public void setInjectionAnalyzer(InjectionAnalyzer injectionAnalyzer) {
        this.injectionAnalyzer = injectionAnalyzer;
    }

    public InjectionAnalyzer getInjectionAnalyzer() {
        return this.injectionAnalyzer;
    }

    @Override
    public void deploy(VFSDeploymentUnit unit, RackApplicationMetaData rackAppMetaData) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        try {
            String beanName = AttachmentUtils.beanName( unit, RackApplicationFactory.class );

            BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder( beanName, RackApplicationFactoryImpl.class.getName() );

            builder.addPropertyMetaData( "rackUpScript", rackAppMetaData.getRackUpScript( rubyAppMetaData.getRoot() ) );

            VirtualFile rackUpScriptLocation = rackAppMetaData.getRackUpScriptFile( rubyAppMetaData.getRoot() );

            if (rackUpScriptLocation == null) {
                rackUpScriptLocation = rubyAppMetaData.getRoot().getChild( SYNTHETIC_CONFIG_RU_NAME );
            }
            builder.addPropertyMetaData( "rackUpFile", rackUpScriptLocation );

            setUpInjections( unit, builder, rubyAppMetaData.getRoot(), rackUpScriptLocation );

            AttachmentUtils.attach( unit, builder.getBeanMetaData() );

            rackAppMetaData.setRackApplicationFactoryName( beanName );

        } catch (Exception e) {
            throw new DeploymentException( e );
        }
    }

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
}
