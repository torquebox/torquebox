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

package org.torquebox.jobs.deployers;

import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.common.util.StringUtils;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: PARSE
 *    In: jobs.yml
 *   Out: ScheduledJobMetaData
 * </pre>
 * 
 * Creates ScheduledJobMetaData instances from jobs.yml
 */
public class JobsYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public JobsYamlParsingDeployer() {
        setSectionName( "jobs" );
        addOutput( ScheduledJobMetaData.class );
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object dataObject) throws DeploymentException {
        Map<String, Map<String, ?>> data = (Map<String, Map<String, ?>>) dataObject;

        log.debug( "Deploying: " + data );

        for (String jobName : data.keySet()) {
            Map<String, ?> jobSpec = data.get( jobName );
            String description = (String) jobSpec.get( "description" );
            String job = (String) jobSpec.get( "job" );
            String cron = (String) jobSpec.get( "cron" );
            Object singleton = jobSpec.get("singleton");
            if (job == null) {
                throw new DeploymentException( "Attribute 'job' must be specified" );
            }

            if (cron == null) {
                throw new DeploymentException( "Attribute 'cron' must be specified" );
            }
            
            if (singleton != null && !(singleton instanceof Boolean)) {
            	throw new DeploymentException(" Attribute 'singleton' must be either true or false." );
            }

            ScheduledJobMetaData jobMetaData = new ScheduledJobMetaData();

            jobMetaData.setName( jobName.toString() );
            jobMetaData.setGroup( unit.getName() );
            if (description != null) {
                jobMetaData.setDescription( description.toString() );
            }
            jobMetaData.setRubyClassName( job.trim() );
            jobMetaData.setCronExpression( cron.trim() );
            jobMetaData.setRubyRequirePath( StringUtils.underscore( job.trim() ) );
            jobMetaData.setSingleton( singleton == null ? false : (Boolean) singleton );
            
            AttachmentUtils.multipleAttach( unit, jobMetaData, jobName );
        }
    }
}
