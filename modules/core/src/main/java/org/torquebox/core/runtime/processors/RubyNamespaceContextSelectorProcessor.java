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

package org.torquebox.core.runtime.processors;

import org.jboss.as.ee.naming.InjectedEENamespaceContextSelector;
import org.jboss.as.naming.NamingStore;
import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;

public class RubyNamespaceContextSelectorProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        
        if ( unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY ) == null ) {
            return;
        }
        
        InjectedEENamespaceContextSelector selector = new InjectedEENamespaceContextSelector();
        ValueService<NamespaceContextSelector> service = new ValueService<NamespaceContextSelector>( new ImmediateValue<NamespaceContextSelector>( selector ) );
        
        String applicationName = unit.getName();
        
        ServiceName appContextServiceName = ContextNames.contextServiceNameOfApplication(applicationName);
        ServiceName moduleContextServiceName = ContextNames.contextServiceNameOfModule(applicationName, applicationName);
        
        final Injector<NamingStore> appInjector = selector.getAppContextInjector();
        final Injector<NamingStore> moduleInjector = selector.getModuleContextInjector();
        final Injector<NamingStore> compInjector = selector.getCompContextInjector();
        final Injector<NamingStore> jbossInjector = selector.getJbossContextInjector();
        final Injector<NamingStore> globalInjector = selector.getGlobalContextInjector();
        
        ServiceName name = CoreServices.appNamespaceContextSelector( unit );
        phaseContext.getServiceTarget().addService( name, service )
            .addDependency(appContextServiceName, NamingStore.class, appInjector)
            .addDependency(moduleContextServiceName, NamingStore.class, moduleInjector)
            .addDependency(moduleContextServiceName, NamingStore.class, compInjector) // intentionally using module for components
            .addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, NamingStore.class, globalInjector)
            .addDependency(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, NamingStore.class, jbossInjector)
            .install();
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
