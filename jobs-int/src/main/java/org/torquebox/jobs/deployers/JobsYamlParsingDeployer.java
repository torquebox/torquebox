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
import org.torquebox.jobs.metadata.RubyJobMetaData;
import org.yaml.snakeyaml.Yaml;

public class JobsYamlParsingDeployer extends AbstractParsingDeployer {

	public JobsYamlParsingDeployer() {
		addOutput(RubyJobMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (unit instanceof VFSDeploymentUnit) {
			deploy((VFSDeploymentUnit) unit);
		}
	}

	protected void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		VirtualFile metaData = unit.getMetaDataFile("jobs.yml");
		if (metaData != null) {
			parse(unit, metaData);
		}
	}

	@SuppressWarnings("unchecked")
	protected void parse(VFSDeploymentUnit unit, VirtualFile file) throws DeploymentException {
		InputStream in = null;
		try {
			in = file.openStream();
			Yaml yaml = new Yaml();
			Map<String, Map<String, String>> results = (Map<String, Map<String, String>>) yaml.load(in);

			for (String jobName : results.keySet()) {
				Map<String, String> jobSpec = results.get(jobName);
				String description = jobSpec.get("description");
				String job = jobSpec.get("job");
				String cron = jobSpec.get("cron");

				if (job == null) {
					throw new DeploymentException( "Attribute 'job' must be specified" );
				}
				
				if (cron == null) {
					throw new DeploymentException( "Attribute 'cron' must be specified" );
				}

				RubyJobMetaData jobMetaData = new RubyJobMetaData();

				jobMetaData.setName(jobName.toString());
				jobMetaData.setGroup(unit.getName());
				if (description != null) {
					jobMetaData.setDescription(description.toString());
				}
				jobMetaData.setRubyClassName(job.toString());
				jobMetaData.setCronExpression(cron.toString().trim());
				unit.addAttachment(RubyJobMetaData.class.getName() + "$" + jobName, jobMetaData, RubyJobMetaData.class);
			}
		} catch (IOException e) {
			throw new DeploymentException(e);
		} finally {
			if ( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					throw new DeploymentException(e);
				}
			}
		}
	}
}