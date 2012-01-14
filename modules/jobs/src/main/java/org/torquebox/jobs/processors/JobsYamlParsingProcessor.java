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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.TimeIntervalUtil;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.util.StringUtils;
import org.torquebox.jobs.ScheduledJobMetaData;

/**
 * Creates ScheduledJobMetaData instances from jobs.yml
 */
public class JobsYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public JobsYamlParsingProcessor() {
        setSectionName( "jobs" );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObject) throws DeploymentUnitProcessingException {
        Map<String, Map<String, ?>> data = (Map<String, Map<String, ?>>) dataObject;

        for (String jobName : data.keySet()) {
            Map<String, ?> jobSpec = data.get( jobName );
            String description = (String) jobSpec.get( "description" );
            String job = (String) jobSpec.get( "job" );
            String cron = (String) jobSpec.get( "cron" );
            Object singleton = jobSpec.get("singleton");
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
            jobMetaData.setSingleton( singleton == null ? false : (Boolean) singleton );
    
            unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMetaData );

            String timeoutStr = jobSpec.containsKey( "job-timeout" ) ?
                jobSpec.get( "job-timeout" ).toString() : null;

            if (timeoutStr != null) {
                TimeIntervalUtil.IntervalData timeout = TimeIntervalUtil.parseInterval( timeoutStr, TimeUnit.MINUTES );
                    
                if (timeout != null) {
                    jobMetaData.setJobTimeout( timeout.interval, timeout.unit );
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
