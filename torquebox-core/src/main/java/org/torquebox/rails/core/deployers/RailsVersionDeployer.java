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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;
import org.torquebox.rails.core.metadata.RailsApplicationMetaData;
import org.torquebox.rails.core.metadata.RailsVersionMetaData;

public class RailsVersionDeployer extends AbstractParsingDeployer {

	private static final Logger log = Logger.getLogger(RailsVersionDeployer.class);

	public RailsVersionDeployer() {
		setInput(RailsApplicationMetaData.class);
		setOutput(RailsVersionMetaData.class);
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		RailsApplicationMetaData railsMetaData = unit.getAttachment(RailsApplicationMetaData.class);
		VirtualFile railsRoot = railsMetaData.getRailsRoot();

		log.debug("Determining version of Rails for " + railsRoot);

		RailsVersionMetaData railsVersionMetaData = determineVendorRailsVersion(railsRoot);
		
		if ( railsVersionMetaData == null ) {
			railsVersionMetaData = determineGemRailsVersion(railsRoot);
		}
		
		if ( railsVersionMetaData == null ) {
			throw new DeploymentException( "Unable to determine Rails version" );
		}
		
		unit.addAttachment(RailsVersionMetaData.class, railsVersionMetaData);
		log.debug("deploying Rails version: " + railsVersionMetaData);
	}

	protected RailsVersionMetaData determineGemRailsVersion(VirtualFile railsRoot) throws DeploymentException {
		VirtualFile configEnvironmentFile = null;
		
		try {
			configEnvironmentFile = railsRoot.getChild( "/config/environment.rb" );
			if (configEnvironmentFile == null || !configEnvironmentFile.exists()) {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
		
		Pattern pattern  = Pattern.compile("^[^#]*RAILS_GEM_VERSION\\s*=\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*");
		
		BufferedReader in = null;
		
		try {
			InputStream inStream = configEnvironmentFile.openStream();
			InputStreamReader inReader = new InputStreamReader(inStream);
			in = new BufferedReader(inReader);
			String line = null;
			while ((line = in.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				System.err.println( "LINE [" + line + "] vs " + pattern );
				if ( matcher.matches() ) {
					System.err.println( "MATCH" );
					String versionStr = matcher.group(1).trim();
					String[] parts = versionStr.split("\\.");
					int major = Integer.parseInt( parts[0] );
					int minor = Integer.parseInt( parts[1] );
					int tiny = 0;
					try {
						tiny = Integer.parseInt( parts[2] );
					} catch (NumberFormatException e) {
						// ignore
					}
					return new RailsVersionMetaData( major, minor, tiny );
				}
			}
		} catch (IOException e) {
			throw new DeploymentException( e );
		}
		return null;
		
	}
	protected RailsVersionMetaData determineVendorRailsVersion(VirtualFile railsRoot) throws DeploymentException {

		VirtualFile railsVersionFile = null;

		try {
			railsVersionFile = railsRoot.getChild("/vendor/rails/railties/lib/rails/version.rb");
			if (railsVersionFile == null || !railsVersionFile.exists()) {
				return null;
			}
		} catch (IOException e) {
			return null;
		}

		Pattern majorPattern = Pattern.compile("^\\s*MAJOR\\s*=\\s*([0-9]+)\\s*$");
		Pattern minorPattern = Pattern.compile("^\\s*MINOR\\s*=\\s*([0-9]+)\\s*$");
		Pattern tinyPattern = Pattern.compile("^\\s*TINY\\s*=\\s*([0-9]+)\\s*$");

		Integer major = null;
		Integer minor = null;
		Integer tiny = null;

		BufferedReader in = null;
		try {
			InputStream inStream = railsVersionFile.openStream();
			InputStreamReader inReader = new InputStreamReader(inStream);
			in = new BufferedReader(inReader);
			String line = null;

			try {
				while ((line = in.readLine()) != null) {
					if (major == null) {
						Matcher matcher = majorPattern.matcher(line);
						if (matcher.matches()) {
							String value = matcher.group(1).trim();
							major = new Integer(value);
						}
					} else if (minor == null) {
						Matcher matcher = minorPattern.matcher(line);
						if (matcher.matches()) {
							String value = matcher.group(1).trim();
							minor = new Integer(value);
						}
					} else if (tiny == null) {
						Matcher matcher = tinyPattern.matcher(line);
						if (matcher.matches()) {
							String value = matcher.group(1).trim();
							tiny = new Integer(value);
						}
					}
				}
			} catch (IOException e) {
				throw new DeploymentException(e);
			}
		} catch (FileNotFoundException e) {
			throw new DeploymentException(e);
		} catch (IOException e) {
			throw new DeploymentException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new DeploymentException(e);
				}
			}
		}
		return new RailsVersionMetaData(major.intValue(), minor.intValue(), tiny.intValue());
	}
}
