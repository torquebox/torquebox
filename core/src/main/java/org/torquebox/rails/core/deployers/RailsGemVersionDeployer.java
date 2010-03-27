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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.torquebox.rails.metadata.RailsApplicationMetaData;
import org.torquebox.rails.metadata.RailsGemVersionMetaData;

public class RailsGemVersionDeployer extends AbstractParsingDeployer {

	//private static final Logger log = Logger.getLogger(RailsGemVersionDeployer.class);

	public RailsGemVersionDeployer() {
		setInput(RailsApplicationMetaData.class);
		setOutput(RailsGemVersionMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		RailsApplicationMetaData railsMetaData = unit.getAttachment(RailsApplicationMetaData.class);
		VirtualFile railsRoot = railsMetaData.getRailsRoot();
		
		log.info( "Rails Root = " + railsRoot );

		VirtualFile vendorRails = railsRoot.getChild("vendor/rails");
		if (vendorRails != null && vendorRails.exists()) {
			return;
		}

		RailsGemVersionMetaData railsVersionMetaData = determineRailsGemVersion(railsRoot);

		unit.addAttachment(RailsGemVersionMetaData.class, railsVersionMetaData);
		log.debug("deploying Rails version: " + railsVersionMetaData);
	}

	protected RailsGemVersionMetaData determineRailsGemVersion(VirtualFile railsRoot) throws DeploymentException {
		VirtualFile configEnvironmentFile = null;

		configEnvironmentFile = railsRoot.getChild("/config/environment.rb");
		
		log.info( "config/environment.rb = " + configEnvironmentFile );
		
		if (configEnvironmentFile == null || !configEnvironmentFile.exists()) {
			return null;
		}

		Pattern pattern = Pattern.compile("^[^#]*RAILS_GEM_VERSION\\s*=\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*");

		BufferedReader in = null;

		try {
			InputStream inStream = configEnvironmentFile.openStream();
			InputStreamReader inReader = new InputStreamReader(inStream);
			in = new BufferedReader(inReader);
			String line = null;
			while ((line = in.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					String versionSpec = matcher.group(1).trim();
					return new RailsGemVersionMetaData(versionSpec);
				}
			}
		} catch (IOException e) {
			throw new DeploymentException(e);
		}
		return new RailsGemVersionMetaData(null);
	}
}
