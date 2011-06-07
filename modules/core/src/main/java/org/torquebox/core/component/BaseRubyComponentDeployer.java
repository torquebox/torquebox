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

package org.torquebox.core.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.injection.analysis.Injectable;
import org.torquebox.core.injection.analysis.InjectionIndex;

public abstract class BaseRubyComponentDeployer implements DeploymentUnitProcessor {

    protected void addInjections(DeploymentPhaseContext phaseContext, ComponentResolver resolver, List<String> injectionPathPrefixes,
            ServiceBuilder<ComponentResolver> builder)
            throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        InjectionIndex index = unit.getAttachment( InjectionIndex.ATTACHMENT_KEY );

        if (index == null) {
            log.warn( "No injection index for " + phaseContext + " //  + resolver " );
            return;
        }

        Set<Injectable> injectables = index.getInjectablesFor( injectionPathPrefixes );

        for (Injectable injectable : injectables) {
            try {
                ServiceName serviceName = injectable.getServiceName( phaseContext );
                builder.addDependency( serviceName, resolver.getInjector( injectable.getKey() ) );
            } catch (Exception e) {
                throw new DeploymentUnitProcessingException( e );
            }
        }

        AttachmentList<Injectable> additionalInjectables = unit.getAttachment( ComponentResolver.ADDITIONAL_INJECTABLES );
        
        if (additionalInjectables != null) {
            for (Injectable injectable : additionalInjectables) {
                try {
                    ServiceName serviceName = injectable.getServiceName( phaseContext );
                    builder.addDependency( serviceName, resolver.getInjector( injectable.getKey() ) );
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
        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        boolean alwaysReload = false;
        if (appMetaData != null) {
            alwaysReload = appMetaData.isDevelopmentMode();
        }

        return new ComponentResolver( alwaysReload );
    }

    protected List<String> defaultInjectionPathPrefixes() {
        List<String> defaults = new ArrayList<String>();
        defaults.add( "app/models/" );
        defaults.add( "lib/" );
        return defaults;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.component.injection" );

}
