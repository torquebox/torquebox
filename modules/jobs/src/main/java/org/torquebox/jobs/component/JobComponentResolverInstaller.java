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

package org.torquebox.jobs.component;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.as.DeploymentNotifier;
import org.torquebox.core.component.BaseRubyComponentDeployer;
import org.torquebox.core.component.ComponentClass;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.jobs.ScheduledJobMetaData;
import org.torquebox.jobs.as.JobsServices;

public class JobComponentResolverInstaller extends BaseRubyComponentDeployer {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<ScheduledJobMetaData> allScheduledJobMetaData = unit.getAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY );

        for (ScheduledJobMetaData jobMetaData : allScheduledJobMetaData) {
            deploy( phaseContext, jobMetaData );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, ScheduledJobMetaData jobMetaData) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ComponentClass instantiator = new ComponentClass();
        instantiator.setClassName( jobMetaData.getRubyClassName() );
        instantiator.setRequirePath( jobMetaData.getRubyRequirePath() );

        ServiceName serviceName = JobsServices.jobComponentResolver( unit, jobMetaData.getRubyClassName() );
        ComponentResolver resolver = new ComponentResolver();
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( JobComponent.class );

        log.info( "Installing Jobs component resolver: " + serviceName );
        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        addInjections( phaseContext, resolver, getInjectionPathPrefixes( phaseContext, jobMetaData.getRubyRequirePath() ), builder );
        builder.setInitialMode( Mode.PASSIVE );
        builder.install();
        
        // Add to our notifier's watch list
        unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName );
    }

    protected List<String> getInjectionPathPrefixes(DeploymentPhaseContext phaseContext, String requirePath) {

        final List<String> prefixes = new ArrayList<String>();

        if (requirePath != null) {
            final DeploymentUnit unit = phaseContext.getDeploymentUnit();
            final ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
            final VirtualFile root = resourceRoot.getRoot();

            final String sourcePath = searchForSourceFile( root, requirePath, true, true, "app/jobs", "lib" );

            if (sourcePath != null) {
                prefixes.add( sourcePath );
            }
        }

        prefixes.add( "lib/" );
        prefixes.add( "app/models/" );

        return prefixes;
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.jobs.component" );

}
