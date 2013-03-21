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

package org.torquebox.core.component.processors;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.projectodd.polyglot.core.as.DeploymentNotifier;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentClass;
import org.torquebox.core.component.ComponentInstantiator;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;

import java.util.Map;

import static org.jboss.msc.service.ServiceController.Mode;

public class ComponentResolverHelper {

    public ComponentResolverHelper(DeploymentPhaseContext phaseContext, ServiceName serviceName) {
        this.serviceName = serviceName;
        this.serviceTarget = phaseContext.getServiceTarget();
        this.unit = phaseContext.getDeploymentUnit();
    }

    public ComponentResolverHelper(ServiceTarget serviceTarget, DeploymentUnit unit, ServiceName serviceName) {
        this.serviceName = serviceName;
        this.serviceTarget = serviceTarget;
        this.unit = unit;
    }

    public ComponentResolverHelper initializeInstantiator(String rubyClassName, String rubyRequirePath) {
        ComponentClass componentClass = new ComponentClass();

        componentClass.setClassName(rubyClassName);
        componentClass.setRequirePath(rubyRequirePath);

        instantiator = componentClass;

        return this;
    }

    public ComponentResolverHelper initializeInstantiator(ComponentInstantiator instantiator) {
        this.instantiator = instantiator;
        return this;
    }

    public ComponentResolverHelper initializeResolver(Class componentWrapperClass, Map<String, Object> config, boolean alwaysNewInstance) throws Exception {
        return initializeResolver(componentWrapperClass, config, alwaysNewInstance, isInDevmode());
    }

    public ComponentResolverHelper initializeResolver(Class componentWrapperClass, Map<String, Object> config, boolean alwaysNewInstance, boolean alwaysReload) throws Exception {
        if (instantiator == null) {
            throw new Exception("Instantiator needs to be initialized before initializing component resolver");
        }

        log.tracef("Initializing component resolver for service '%s'...", serviceName.getSimpleName());

        componentResolver = new ComponentResolver(alwaysReload);

        componentResolver.setAlwaysNewInstance(alwaysNewInstance);
        componentResolver.setComponentInstantiator(instantiator);
        componentResolver.setComponentName(serviceName.getCanonicalName());
        componentResolver.setComponentWrapperClass(componentWrapperClass);
        componentResolver.setInitializeParams(config);

        return this;
    }

    public void installService(Mode mode) throws Exception {
        if (componentResolver == null) {
            throw new Exception("Component resolver needs to be initialized before initializing service");
        }

        initializeService();
        addNamespaceContext();

        builder.setInitialMode(mode);
        builder.install();

        addToNotifierWatchList();
    }

    private void initializeService() throws Exception {
        log.tracef("Initializing component resolver service for service '%s'...", serviceName.getSimpleName());

        componentResolverService = new ComponentResolverService(componentResolver);
        builder = serviceTarget.addService(serviceName, componentResolverService);
    }

    private void addNamespaceContext() {
        log.tracef("Adding namespace context dependency to component resolver service '%s'...", serviceName.getSimpleName());

        ServiceName namespaceContextSelectorName = CoreServices.appNamespaceContextSelector(unit);
        builder.addDependency(namespaceContextSelectorName, NamespaceContextSelector.class, componentResolverService.getNamespaceContextSelectorInjector());
    }

    private void addToNotifierWatchList() {
        log.trace("Adding service to notifier watch list...");

        // Add to our notifier's watch list
        unit.addToAttachmentList(DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName);
    }

    private boolean isInDevmode() {
        RubyAppMetaData appMetaData = unit.getAttachment(RubyAppMetaData.ATTACHMENT_KEY);

        boolean alwaysReload = false;

        if (appMetaData != null) {
            alwaysReload = appMetaData.isDevelopmentMode();
        }

        return alwaysReload;
    }

    private ComponentResolverService componentResolverService;
    private ServiceBuilder<ComponentResolver> builder;
    private ComponentResolver componentResolver;
    private ComponentInstantiator instantiator;

    private ServiceName serviceName;
    private ServiceTarget serviceTarget;
    private DeploymentUnit unit;

    private static final Logger log = Logger.getLogger("org.torquebox.core.component");
}
