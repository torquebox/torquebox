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
import org.jruby.util.ByteList;
import org.jvyamlb.YAML;
import org.torquebox.jobs.metadata.RubyJobMetaData;

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
			Map<ByteList, Map<ByteList, ByteList>> results = (Map<ByteList, Map<ByteList, ByteList>>) YAML.load(in);

			for (ByteList jobName : results.keySet()) {
				Map<ByteList, ByteList> jobSpec = results.get(jobName);
				ByteList description = jobSpec.get(ByteList.create("description"));
				ByteList job = jobSpec.get(ByteList.create("job"));
				ByteList cron = jobSpec.get(ByteList.create("cron"));

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