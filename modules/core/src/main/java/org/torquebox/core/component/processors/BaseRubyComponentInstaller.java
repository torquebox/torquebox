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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.core.injection.analysis.Injectable;
import org.torquebox.core.injection.analysis.InjectionIndex;

public abstract class BaseRubyComponentInstaller implements DeploymentUnitProcessor {

    protected void addNamespaceContext(DeploymentPhaseContext phaseContext, ComponentResolverService resolverService, ServiceBuilder<ComponentResolver> builder) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ServiceName selectorName = CoreServices.appNamespaceContextSelector( unit );
        builder.addDependency( selectorName, NamespaceContextSelector.class, resolverService.getNamespaceContextSelectorInjector() );
    }

    protected void addInjections(DeploymentPhaseContext phaseContext, ComponentResolver resolver, List<String> injectionPathPrefixes,
            ServiceBuilder<ComponentResolver> builder)
            throws DeploymentUnitProcessingException {
        addInjections( phaseContext, resolver, injectionPathPrefixes, builder, new String[] {} );
    }

    protected void addInjections(DeploymentPhaseContext phaseContext, ComponentResolver resolver, List<String> injectionPathPrefixes,
            ServiceBuilder<ComponentResolver> builder, String... typesToExclude)
            throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        InjectionIndex index = unit.getAttachment( InjectionIndex.ATTACHMENT_KEY );
        List<String> typesToExcludeList = Arrays.asList( typesToExclude );

        if (index != null) {
            Set<Injectable> injectables = index.getInjectablesFor( injectionPathPrefixes );

            for (Injectable injectable : injectables) {
                if (typesToExcludeList.contains( injectable.getType() )) {
                    continue; // Used by services to prevent circular injection
                              // dependencies
                }
                try {
                    ServiceName serviceName = injectable.getServiceName( phaseContext.getServiceTarget(), phaseContext.getDeploymentUnit() );
                    builder.addDependency( serviceName, resolver.getInjector( injectable.getKey() ) );
                } catch (Exception e) {
                    throw new DeploymentUnitProcessingException( e );
                }
            }
        }

        AttachmentList<Injectable> additionalInjectables = unit.getAttachment( ComponentResolver.ADDITIONAL_INJECTABLES );

        if (additionalInjectables != null) {
            for (Injectable injectable : additionalInjectables) {
                try {
                    ServiceName serviceName = injectable.getServiceName( phaseContext.getServiceTarget(), phaseContext.getDeploymentUnit() );
                    if (serviceName != null) {
                        builder.addDependency( serviceName, resolver.getInjector( injectable.getKey() ) );
                    } else if (!injectable.isOptional()) {
                        log.error( "Unable to inject: " + injectable.getName() );
                    }
                } catch (Exception e) {
                    throw new DeploymentUnitProcessingException( e );
                }
            }
        }
    }

    protected String searchForSourceFile(VirtualFile root, String requirePath, boolean searchRoot, boolean searchAppDirRoots, String... roots) {

        final String filePath = requirePath + ".rb";

        if (searchRoot) {
            final VirtualFile candidate = root.getChild( filePath );
            if (candidate.exists()) {
                return candidate.getPathNameRelativeTo( root );
            }
        }

        for (String eachRoot : roots) {
            final VirtualFile searchableRoot = root.getChild( eachRoot );
            final VirtualFile candidate = searchableRoot.getChild( filePath );

            if (candidate.exists()) {
                return candidate.getPathNameRelativeTo( root );
            }
        }

        if (searchAppDirRoots) {
            final VirtualFile appDir = root.getChild( "app" );

            for (VirtualFile eachChild : appDir.getChildren()) {
                final VirtualFile candidate = eachChild.getChild( filePath );

                if (candidate.exists()) {
                    return candidate.getPathNameRelativeTo( root );
                }
            }
        }

        return null;
    }

    protected ComponentResolver createComponentResolver(DeploymentUnit unit) {
        RubyAppMetaData appMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        boolean alwaysReload = false;
        if (appMetaData != null) {
            alwaysReload = appMetaData.isDevelopmentMode();
        }

        return new ComponentResolver( alwaysReload );
    }

    protected List<String> defaultInjectionPathPrefixes() {
        List<String> defaults = new ArrayList<String>();
        defaults.add( "app/" );
        defaults.add( "lib/" );
        defaults.add( "models/" );
        defaults.add( "." );
        return defaults;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.component.injection" );

}
