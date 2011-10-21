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

package org.torquebox.core.runtime.processors;

import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServer;

import org.jboss.as.jmx.MBeanRegistrationService;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.jmx.ObjectNameFactory;
import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.core.as.DeploymentNotifier;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.BasicRubyRuntimePoolMBean;
import org.torquebox.core.runtime.DefaultRubyRuntimePool;
import org.torquebox.core.runtime.DefaultRubyRuntimePoolMBean;
import org.torquebox.core.runtime.DefaultRubyRuntimePoolService;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.core.runtime.RubyRuntimeFactory;
import org.torquebox.core.runtime.RubyRuntimePoolStartService;
import org.torquebox.core.runtime.SharedRubyRuntimeFactoryPoolService;
import org.torquebox.core.runtime.SharedRubyRuntimePool;

/**
 * <pre>
 * Stage: REAL
 *    In: PoolMetaData, DeployerRuby
 *   Out: RubyRuntimePool
 * </pre>
 * 
 * Creates the proper RubyRuntimePool as specified by the PoolMetaData
 */
public class RuntimePoolInstaller implements DeploymentUnitProcessor {

    public RuntimePoolInstaller() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<PoolMetaData> allAttachments = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );

        for (PoolMetaData each : allAttachments) {
            deploy( phaseContext, each );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, final PoolMetaData poolMetaData) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        final RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );

        if (poolMetaData.isShared()) {
            SharedRubyRuntimePool pool = new SharedRubyRuntimePool();
            
            pool.setName( poolMetaData.getName() );
            //pool.setDeferUntilRequested( poolMetaData.isDeferUntilRequested() );
            //pool.setStartAsynchronously( poolMetaData.isStartAsynchronously() );
            
            SharedRubyRuntimeFactoryPoolService service = new SharedRubyRuntimeFactoryPoolService( pool );

            ServiceName name = CoreServices.runtimePoolName( unit, pool.getName() );

            phaseContext.getServiceTarget().addService( name, service )
                    .addDependency( CoreServices.runtimeFactoryName( unit ), RubyRuntimeFactory.class, service.getRubyRuntimeFactoryInjector() )
                    .addDependency( CoreServices.appNamespaceContextSelector( unit ), NamespaceContextSelector.class, service.getNamespaceContextSelectorInjector() )
                    .install();

            unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, name );

            phaseContext.getServiceTarget().addService( name.append( "START" ), new RubyRuntimePoolStartService( pool ) )
                    .addDependency( name )
                    .setInitialMode( poolMetaData.isStartAsynchronously() ? Mode.PASSIVE : Mode.ON_DEMAND )
                    .install();

            String mbeanName = ObjectNameFactory.create( "torquebox.pools", new Hashtable<String, String>() {
                {
                    put( "app", rubyAppMetaData.getApplicationName() );
                    put( "name", poolMetaData.getName() );
                }
            } ).toString();

            MBeanRegistrationService<BasicRubyRuntimePoolMBean> mbeanService = new MBeanRegistrationService<BasicRubyRuntimePoolMBean>( mbeanName );
            phaseContext.getServiceTarget().addService( name.append( "mbean" ), mbeanService )
                    .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                    .addDependency( name, BasicRubyRuntimePoolMBean.class, mbeanService.getValueInjector() )
                    .setInitialMode( Mode.PASSIVE )
                    .install();

        } else {
            DefaultRubyRuntimePool pool = new DefaultRubyRuntimePool();
            
            pool.setName( poolMetaData.getName() );
            pool.setMinimumInstances( poolMetaData.getMinimumSize() );
            pool.setMaximumInstances( poolMetaData.getMaximumSize() );
            pool.setDeferUntilRequested( poolMetaData.isDeferUntilRequested() );
            pool.setStartAsynchronously( poolMetaData.isStartAsynchronously() );
        
            DefaultRubyRuntimePoolService service = new DefaultRubyRuntimePoolService( pool );

            ServiceName name = CoreServices.runtimePoolName( unit, pool.getName() );
            phaseContext.getServiceTarget().addService( name, service )
                    .addDependency( CoreServices.runtimeFactoryName( unit ), RubyRuntimeFactory.class, service.getRubyRuntimeFactoryInjector() )
                    .addDependency( CoreServices.appNamespaceContextSelector( unit ), NamespaceContextSelector.class, service.getNamespaceContextSelectorInjector() )
                    .install();

            unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, name );

            phaseContext.getServiceTarget().addService( name.append( "START" ), new RubyRuntimePoolStartService( pool ) )
                    .addDependency( name )
                    .setInitialMode( poolMetaData.isStartAsynchronously() ? Mode.PASSIVE : Mode.ON_DEMAND )
                    .install();
            
            String mbeanName = ObjectNameFactory.create( "torquebox.pools", new Hashtable<String, String>() {
                {
                    put( "app", rubyAppMetaData.getApplicationName() );
                    put( "name", poolMetaData.getName() );
                }
            } ).toString();
            
            MBeanRegistrationService<DefaultRubyRuntimePoolMBean> mbeanService = new MBeanRegistrationService<DefaultRubyRuntimePoolMBean>( mbeanName );
            phaseContext.getServiceTarget().addService( name.append( "mbean" ), mbeanService )
                    .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                    .addDependency( name, DefaultRubyRuntimePoolMBean.class, mbeanService.getValueInjector() )
                    .setInitialMode( Mode.PASSIVE )
                    .install();
        }
    }

    @Override
    public void undeploy(org.jboss.as.server.deployment.DeploymentUnit context) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core.pool" );
}
