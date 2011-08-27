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

package org.torquebox.messaging.component;

import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServer;

import org.jboss.as.jmx.MBeanRegistrationService;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.jmx.ObjectNameFactory;
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
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.DeploymentNotifier;
import org.torquebox.core.component.BaseRubyComponentDeployer;
import org.torquebox.core.component.ComponentClass;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.messaging.as.MessagingServices;

public class MessageProcessorComponentResolverInstaller extends BaseRubyComponentDeployer {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<MessageProcessorMetaData> allMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY );

        if (allMetaData == null || allMetaData.isEmpty()) {
            return;
        }

        for (MessageProcessorMetaData each : allMetaData) {
            deploy( phaseContext, each );

        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, final MessageProcessorMetaData metaData) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ComponentClass instantiator = new ComponentClass();

        instantiator.setClassName( metaData.getRubyClassName() );
        instantiator.setRequirePath( metaData.getRubyRequirePath() );

        ServiceName serviceName = MessagingServices.messageProcessorComponentResolver( unit, metaData.getName() );
        ComponentResolver resolver = createComponentResolver( unit );
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( MessageProcessorComponent.class );
        resolver.setInitializeParams( metaData.getRubyConfig() );

        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.ON_DEMAND );
        addInjections( phaseContext, resolver, getInjectionPathPrefixes( phaseContext, metaData.getRubyRequirePath() ), builder );
        builder.install();

        // Add to our notifier's watch list
        unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName );
        

    }

    protected List<String> getInjectionPathPrefixes(DeploymentPhaseContext phaseContext, String requirePath) {

        final List<String> prefixes = defaultInjectionPathPrefixes();

        if (requirePath != null) {

            final DeploymentUnit unit = phaseContext.getDeploymentUnit();
            final ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
            final VirtualFile root = resourceRoot.getRoot();

            final String sourcePath = searchForSourceFile( root, requirePath, true, true, "app/tasks", "app/processors", "lib" );

            if (sourcePath != null) {
                prefixes.add( sourcePath );
            }
        }

        return prefixes;
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.messaging.component" );
}
