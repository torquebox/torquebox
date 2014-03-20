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

package org.torquebox.jobs.processors;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.util.StringUtils;
import org.torquebox.jobs.JobSchedulerMetaData;
import org.torquebox.jobs.ScheduledJobMetaData;

/**
 * Creates ScheduledJobMetaData instances from jobs.yml
 */
public class JobsYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public JobsYamlParsingProcessor() {
        setSectionName( "jobs" );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObject) throws DeploymentUnitProcessingException {
        Map<String, ?> data = (Map<String, ?>) dataObject;
        
        JobSchedulerMetaData schedulerMetaData = new JobSchedulerMetaData();
        unit.putAttachment( JobSchedulerMetaData.ATTACHMENT_KEY, schedulerMetaData );
        
        for (String jobName : data.keySet()) {
            Object datum = data.get( jobName );
            if ("concurrency".equals( jobName ) &&
                    !(datum instanceof Map)) {
                
                if (!(datum instanceof Integer)) {
                    throw new DeploymentUnitProcessingException( "Attribute 'concurrency' must be an integer." );
                }
                schedulerMetaData.setThreadCount( (Integer)datum );
                                
            } else {
                
                Map<String, ?> jobSpec = (Map<String, ?>)datum;
                String description = (String) jobSpec.get( "description" );
                String job = (String) jobSpec.get( "job" );
                String cron = (String) jobSpec.get( "cron" );
                Object singleton = jobSpec.get("singleton");
                Object stopped = jobSpec.get("stopped");
                Map<String, Object> params = (Map<String, Object>)jobSpec.get( "config" );

                if (job == null) {
                    throw new DeploymentUnitProcessingException( "Attribute 'job' must be specified" );
                }

                if (cron == null) {
                    throw new DeploymentUnitProcessingException( "Attribute 'cron' must be specified" );
                }

                if (singleton != null && !(singleton instanceof Boolean)) {
                    throw new DeploymentUnitProcessingException(" Attribute 'singleton' must be either true or false." );
                }

                if (stopped != null && !(stopped instanceof Boolean)) {
                    throw new DeploymentUnitProcessingException(" Attribute 'stopped' must be either true or false." );
                }

                ScheduledJobMetaData jobMetaData = new ScheduledJobMetaData();

                jobMetaData.setName( jobName.toString() );
                jobMetaData.setGroup( unit.getName() );
                if (description != null) {
                    jobMetaData.setDescription( description.toString() );
                }
                jobMetaData.setRubyClassName( job.trim() );
                jobMetaData.setCronExpression( cron.trim() );
                jobMetaData.setParameters( params );
                jobMetaData.setRubyRequirePath( StringUtils.underscore( job.trim() ) );
                jobMetaData.setSingleton( singleton == null ? true : (Boolean) singleton );

                if (stopped != null) {
                    jobMetaData.setStopped( (Boolean) stopped );
                }

                unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMetaData );

                String timeoutStr = jobSpec.containsKey( "timeout" ) ?
                        jobSpec.get( "timeout" ).toString() : null;

                jobMetaData.setTimeout( TimeInterval.parseInterval( timeoutStr, TimeUnit.SECONDS ) );
                
            }
        }
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
