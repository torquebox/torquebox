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

package org.torquebox.jobs.processors;

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
import org.projectodd.polyglot.core.util.ClusterUtil;
import org.projectodd.polyglot.jobs.BaseJobScheduler;
import org.projectodd.polyglot.jobs.BaseScheduledJob;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.util.StringUtils;
import org.torquebox.jobs.ScheduledJob;
import org.torquebox.jobs.ScheduledJobMBean;
import org.torquebox.jobs.ScheduledJobMetaData;
import org.torquebox.jobs.as.JobsServices;

/**
 * <pre>
 * Stage: REAL
 *    In: ScheduledJobMetaData
 *   Out: ScheduledJob
 * </pre>
 * 
 * Creates objects from metadata
 */
public class ScheduledJobInstaller implements DeploymentUnitProcessor {

    public ScheduledJobInstaller() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<ScheduledJobMetaData> allJobMetaData = unit.getAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY );

        for (ScheduledJobMetaData metaData : allJobMetaData) {
            deploy( phaseContext, metaData );
        }
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    protected void deploy(DeploymentPhaseContext phaseContext, final ScheduledJobMetaData metaData) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ScheduledJob job = new ScheduledJob( 
                metaData.getGroup(),
                metaData.getName(),
                metaData.getDescription(),
                metaData.getCronExpression(),
                metaData.isSingleton(),
                metaData.getRubyClassName()
                );

        ServiceName serviceName = JobsServices.scheduledJob( unit, metaData.getName() );

        ServiceBuilder<BaseScheduledJob> builder = phaseContext.getServiceTarget().addService( serviceName, job );
        builder.addDependency( CoreServices.runtimePoolName( unit, "jobs" ), RubyRuntimePool.class, job.getRubyRuntimePoolInjector() );
        builder.addDependency( JobsServices.jobComponentResolver( unit, metaData.getName() ), ComponentResolver.class, job.getComponentResolverInjector() );
        builder.addDependency( JobsServices.jobScheduler( unit, metaData.isSingleton() && ClusterUtil.isClustered( phaseContext ) ), BaseJobScheduler.class, job.getJobSchedulerInjector() );

        builder.setInitialMode( Mode.PASSIVE );
        builder.install();

        final RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );

        String mbeanName = ObjectNameFactory.create( "torquebox.jobs", new Hashtable<String, String>() {
            {
                put( "app", rubyAppMetaData.getApplicationName() );
                put( "name", StringUtils.underscore( metaData.getName() ) );
            }
        } ).toString();

        MBeanRegistrationService<ScheduledJobMBean> mbeanService = new MBeanRegistrationService<ScheduledJobMBean>( mbeanName );
        phaseContext.getServiceTarget().addService( serviceName.append( "mbean" ), mbeanService )
                .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                .addDependency( serviceName, ScheduledJobMBean.class, mbeanService.getValueInjector() )
                .setInitialMode( Mode.PASSIVE )
                .install();

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
