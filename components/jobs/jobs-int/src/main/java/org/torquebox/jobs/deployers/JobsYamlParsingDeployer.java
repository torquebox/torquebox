/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.common.util.StringUtils;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.yaml.snakeyaml.Yaml;

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
        setSectionName("jobs");
        addOutput(ScheduledJobMetaData.class);
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object dataObject) throws DeploymentException {
        Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) dataObject;

        if (data != null) {

            for (String jobName : data.keySet()) {
                Map<String, String> jobSpec = data.get(jobName);
                String description = jobSpec.get("description");
                String job = jobSpec.get("job");
                String cron = jobSpec.get("cron");

                if (job == null) {
                    throw new DeploymentException("Attribute 'job' must be specified");
                }

                if (cron == null) {
                    throw new DeploymentException("Attribute 'cron' must be specified");
                }

                ScheduledJobMetaData jobMetaData = new ScheduledJobMetaData();

                jobMetaData.setName(jobName.toString());
                jobMetaData.setGroup(unit.getName());
                if (description != null) {
                    jobMetaData.setDescription(description.toString());
                }
                jobMetaData.setRubyClassName(job.trim());
                jobMetaData.setCronExpression(cron.trim());
                jobMetaData.setRubyRequirePath(StringUtils.underscore(job.trim()));
                AttachmentUtils.multipleAttach(unit, jobMetaData, jobName);
            }
        }
    }
}