/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.services.processors;

import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServer;

import org.jboss.as.jmx.MBeanRegistrationService;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.jmx.ObjectNameFactory;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.ImmediateValue;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.projectodd.polyglot.hasingleton.HASingleton;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RestartableRubyRuntimePool;
import org.torquebox.core.util.StringUtils;
import org.torquebox.services.RubyService;
import org.torquebox.services.RubyServiceCreate;
import org.torquebox.services.RubyServiceMBean;
import org.torquebox.services.RubyServiceStart;
import org.torquebox.services.ServiceMetaData;
import org.torquebox.services.as.ServicesServices;
import org.torquebox.services.injection.InjectableService;

public class ServiceInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        List<ServiceMetaData> allServiceMetaData = unit.getAttachmentList( ServiceMetaData.ATTACHMENTS_KEY );
        
        for (ServiceMetaData serviceMetaData : allServiceMetaData) {
            deploy( phaseContext, serviceMetaData );
        }
    }
    
    protected void deploy(DeploymentPhaseContext phaseContext, final ServiceMetaData serviceMetaData) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        ServiceName serviceCreateName = ServicesServices.serviceCreateRubyService( unit, serviceMetaData.getName() );
        ServiceName serviceStartName = ServicesServices.serviceStartRubyService( unit, serviceMetaData.getName() );
        RubyService service = new RubyService( serviceMetaData.getName() );
        
        RubyServiceCreate serviceCreate = new RubyServiceCreate( service );
        ServiceBuilder<RubyService> builderCreate = phaseContext.getServiceTarget().addService( serviceCreateName, serviceCreate );
        builderCreate.addDependency( ServicesServices.serviceComponentResolver( unit, serviceMetaData.getName() ), ComponentResolver.class, serviceCreate.getComponentResolverInjector() );
        builderCreate.addDependency( CoreServices.runtimePoolName( unit, "services" ), RestartableRubyRuntimePool.class, serviceCreate.getRubyRuntimePoolInjector() );
        builderCreate.setInitialMode( Mode.PASSIVE );
        builderCreate.install();
        
        RubyServiceStart serviceStart = new RubyServiceStart();
        ServiceBuilder<RubyService> builderStart = phaseContext.getServiceTarget().addService( serviceStartName, serviceStart );
        builderStart.addDependency( serviceCreateName, RubyService.class, serviceStart.getRubyServiceInjector() );
        
        if ( serviceMetaData.isRequiresSingleton() ) {
            builderStart.addDependency( HASingleton.serviceName( unit, "global" ) );
        }
        
        builderStart.setInitialMode( Mode.PASSIVE );
        builderStart.install();
        
        InjectableService injectableService = new InjectableService( service );
        phaseContext.getServiceTarget().addService( ServicesServices.serviceInjectableService( unit, serviceMetaData.getName() ), injectableService )
            .addDependencies( serviceStartName )
            .setInitialMode( Mode.PASSIVE )
            .install();
        
        final RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        
        String mbeanName = ObjectNameFactory.create( "torquebox.services", new Hashtable<String, String>() {
            {
                put( "app", rubyAppMetaData.getApplicationName() );
                put( "name", StringUtils.underscore( serviceMetaData.getName() ) );
            }
        } ).toString();

        ServiceName mbeanServiceName = serviceStartName.append( "mbean" );
        MBeanRegistrationService<RubyServiceMBean> mbeanService = new MBeanRegistrationService<RubyServiceMBean>( mbeanName, mbeanServiceName, new ImmediateValue<RubyServiceMBean>( service ) );
        phaseContext.getServiceTarget().addService( mbeanServiceName, mbeanService )
                .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                .setInitialMode( Mode.PASSIVE )
                .install(); 
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.services" );

}
