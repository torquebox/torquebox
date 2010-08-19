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
package org.torquebox.rails.deployers;

import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.metadata.EnvironmentMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;
import org.yaml.snakeyaml.Yaml;

public class RailsEnvYamlParsingDeployer extends AbstractDeployer {
	
	public static final String RAILS_ENV_KEY = "RAILS_ENV";
	
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
				EnvironmentMetaData envMetaData = unit.getAttachment( EnvironmentMetaData.class );
				if ( envMetaData == null ) {
					envMetaData = new EnvironmentMetaData();
					String railsEnv = railsAppMetaData.getRailsEnv();
					if ( railsEnv != null ) {
						envMetaData.setEnvironmentName( railsEnv );
						envMetaData.setDevelopmentMode( railsEnv.equals( "development" ) );
					}
				}
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
			Yaml yaml = new Yaml();
			Map<String, String> parsed = (Map<String, String>) yaml.load(in);

			String railsEnv = parsed.get( RAILS_ENV_KEY );

			if (railsEnv != null ) {
				railsEnv = railsEnv.trim();
				if ( railsEnv.equals( "" ) ) {
					railsEnv = "development";
				}
			} else {
				railsEnv = "development";
			}

			RailsApplicationMetaData railsMetaData = root;

			if (railsMetaData == null) {
				railsMetaData = new RailsApplicationMetaData(unit.getRoot(), railsEnv);
			} else {
				if (railsMetaData.getRailsEnv() == null) {
					railsMetaData.setRailsEnv(railsEnv);
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
