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
package org.torquebox.rails.core.deployers;

import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.util.ByteList;
import org.jvyamlb.YAML;
import org.torquebox.rails.metadata.RailsApplicationMetaData;

public class RailsEnvYamlParsingDeployer extends AbstractDeployer {
	
	public static final ByteList RAILS_ENV_KEY = ByteList.create( "RAILS_ENV" );
	
	public RailsEnvYamlParsingDeployer() {
		setStage(DeploymentStages.PARSE);
		addInput(RailsApplicationMetaData.class);
		addOutput(RailsApplicationMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		if (unit instanceof VFSDeploymentUnit) {
			deploy((VFSDeploymentUnit) unit);
		}
	}

	public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
		VirtualFile file = unit.getMetaDataFile("rails-env.yml");

		if (file != null) {
			try {
				RailsApplicationMetaData railsAppMetaData = unit.getAttachment(RailsApplicationMetaData.class);
				railsAppMetaData = parse(unit, file, railsAppMetaData);
				unit.addAttachment(RailsApplicationMetaData.class, railsAppMetaData);
			} catch (Exception e) {
				throw new DeploymentException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected RailsApplicationMetaData parse(VFSDeploymentUnit unit, VirtualFile file, RailsApplicationMetaData root) throws Exception {
		InputStream in = null;
		try {
			in = file.openStream();
			Map<ByteList, ByteList> parsed = (Map<ByteList, ByteList>) YAML.load(in);

			ByteList railsEnv = parsed.get( RAILS_ENV_KEY );

			String railsEnvStr = "development";
			if (railsEnv != null ) {
				railsEnvStr = railsEnv.toString().trim();
				if ( railsEnvStr.equals( "" ) ) {
					railsEnvStr = "development";
				}
			}

			RailsApplicationMetaData railsMetaData = root;

			if (railsMetaData == null) {
				railsMetaData = new RailsApplicationMetaData(unit.getRoot(), railsEnvStr);
			} else {
				if (railsMetaData.getRailsEnv() == null) {
					railsMetaData.setRailsEnv(railsEnvStr);
				}
			}
			return railsMetaData;
		} finally {
			if ( in != null ) {
				in.close();
			}
		}
	}
}
