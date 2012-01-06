/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.injection.analysis.processors;

import java.io.IOException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.injection.analysis.InjectableHandlerRegistry;
import org.torquebox.core.injection.analysis.InjectionAnalyzer;
import org.torquebox.core.injection.analysis.InjectionIndex;
import org.torquebox.core.injection.analysis.RuntimeInjectionAnalyzer;
import org.torquebox.core.runtime.RubyRuntimeMetaData;

/**
 * Processor which scans Ruby application deployments, building an
 * {@link InjectionIndex} for the entire deployment.
 * 
 * <p>
 * This processor causes no injection to occur, but rather builds and attaches
 * the <code>InjectionIndex</code> to the unit.
 * </p>
 * 
 * @see InjectionIndex
 * @see InjectionIndex#ATTACHMENT_KEY
 * 
 * @author Bob McWhirter
 */
public class InjectionIndexingProcessor implements DeploymentUnitProcessor {

    private static final String[] INJECTION_WHITELIST = { "app", "lib", "models", "helpers" };

    public InjectionIndexingProcessor(InjectableHandlerRegistry registry) {
        this.registry = registry;
        this.injectionAnalyzer = new InjectionAnalyzer( this.registry );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        if (runtimeMetaData == null) {
            return;
        }

        deployRuntimeInjectionAnalyzer( phaseContext );

        InjectionIndex index = unit.getAttachment( InjectionIndex.ATTACHMENT_KEY );

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        if (index == null) {
            index = new InjectionIndex( root );
            unit.putAttachment( InjectionIndex.ATTACHMENT_KEY, index );
        }

        InjectionAnalyzer analyzer = getAnalyzer();

        for (VirtualFile each : root.getChildren()) {
            if (shouldProcess( each )) {
                try {
                    analyzer.analyzeRecursively( index, each, runtimeMetaData.getVersion() );
                } catch (IOException e) {
                    log.error( "Error processing file: " + each );
                }
            }
        }
    }

    private void deployRuntimeInjectionAnalyzer(DeploymentPhaseContext phaseContext) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ServiceName serviceName = CoreServices.runtimeInjectionAnalyzerName( unit );
        RuntimeInjectionAnalyzer runtimeAnalyzer = new RuntimeInjectionAnalyzer( phaseContext.getServiceRegistry(), phaseContext.getServiceTarget(),
                phaseContext.getDeploymentUnit(), this.injectionAnalyzer );
        Service<RuntimeInjectionAnalyzer> service = new ValueService<RuntimeInjectionAnalyzer>( new ImmediateValue<RuntimeInjectionAnalyzer>( runtimeAnalyzer ) );

        phaseContext.getServiceTarget().addService( serviceName, service ).install();

    }

    protected boolean shouldProcess(VirtualFile dir) {
        if (!dir.isDirectory()) {
            return true;
        }
        else {
            for (String element : INJECTION_WHITELIST) {
                if (element.equals( dir.getName() )) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    public InjectableHandlerRegistry getInjectableHandlerRegistry() {
        return this.registry;
    }

    protected synchronized InjectionAnalyzer getAnalyzer() {
        return this.injectionAnalyzer;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.injection.analysis" );

    private InjectableHandlerRegistry registry;
    private InjectionAnalyzer injectionAnalyzer;

}
