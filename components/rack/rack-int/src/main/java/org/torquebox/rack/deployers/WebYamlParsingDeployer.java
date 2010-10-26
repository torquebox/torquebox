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
package org.torquebox.rack.deployers;

import java.util.List;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.yaml.snakeyaml.Yaml;

public class WebYamlParsingDeployer extends AbstractVFSParsingDeployer<RackApplicationMetaData> {

	public WebYamlParsingDeployer() {
		super(RackApplicationMetaData.class);
		setName("web.yml");
	}

    protected boolean allowsReparse()
    {
        return true;
    }

	@SuppressWarnings("unchecked")
	protected RackApplicationMetaData parse(VFSDeploymentUnit unit, VirtualFile file, RackApplicationMetaData root) throws Exception {
		Yaml yaml = new Yaml();
		Map<String, Object> web = (Map<String, Object>) yaml.load(file.openStream());

		if (web == null) {
			throw new DeploymentException("unable to parse: " + file);
		}

		return parse(unit, web, root);
	}

	@SuppressWarnings("unchecked")
	public static RackApplicationMetaData parse(VFSDeploymentUnit unit, Map<String, Object> web, RackApplicationMetaData rackMetaData) {

		if (web == null) {
			return null;
		}

		if (rackMetaData == null) {
			rackMetaData = new RackApplicationMetaData();
		}

		if (rackMetaData.getHosts().isEmpty()) {
			Object hosts = web.get("host");

			if (hosts != null) {
				if (hosts instanceof List) {
					List<String> hostList = (List<String>) hosts;
					for (String each : hostList) {
						rackMetaData.addHost(each);
					}
				} else {
					rackMetaData.addHost(hosts.toString());
				}
			} else {
				rackMetaData.addHost( "localhost" );
			}
		}

		if (rackMetaData.getContextPath() == null) {
			String context = (String) web.get("context");

			if (context == null) {
				context = "/";
			} else {
				context = context.trim();
			}

			rackMetaData.setContextPath(context);
		}

		if (rackMetaData.getStaticPathPrefix() == null) {
			String staticPathPrefix = (String) web.get("static");
			if (staticPathPrefix == null) {
				staticPathPrefix = "/public";
			}
			rackMetaData.setStaticPathPrefix(staticPathPrefix);
		}

		return rackMetaData;
	}

}
