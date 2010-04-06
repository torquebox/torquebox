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

import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.metadata.RackWebApplicationMetaData;
import org.torquebox.rack.spi.RackApplicationPool;
import org.yaml.snakeyaml.Yaml;

public class WebYamlParsingDeployer extends AbstractVFSParsingDeployer<RackWebApplicationMetaData> {

	public WebYamlParsingDeployer() {
		super(RackWebApplicationMetaData.class);
		setName("web.yml");
	}

	@SuppressWarnings("unchecked")
	protected RackWebApplicationMetaData parse(VFSDeploymentUnit unit, VirtualFile file, RackWebApplicationMetaData root) throws Exception {
		Yaml yaml = new Yaml();
		Map<String, String> web = (Map<String, String>) yaml.load(file.openStream());

		if (web == null) {
			throw new DeploymentException("unable to parse: " + file);
		}

		return parse(unit, web);
	}

	protected static RackWebApplicationMetaData parse(VFSDeploymentUnit unit, Map<String, String> web) {

		if (web == null) {
			return null;
		}

		RackWebApplicationMetaData webMetaData = unit.getAttachment(RackWebApplicationMetaData.class);

		if (webMetaData == null) {
			webMetaData = new RackWebApplicationMetaData();
			unit.addAttachment(RackWebApplicationMetaData.class, webMetaData);
		}

		if (webMetaData.getHost() == null) {
			String host = web.get("host");

			if (host == null) {
				host = "localhost";
			} else {
				host = host.trim();
				if (host.equals("")) {
					host = "localhost";
				}
			}

			webMetaData.setHost(host);
		}

		if (webMetaData.getContext() == null) {
			String context = web.get("context");

			if (context == null) {
				context = "/";
			} else {
				context = context.trim();
			}

			webMetaData.setContext(context);
		}

		if (webMetaData.getStaticPathPrefix() == null) {
			String staticPathPrefix = web.get("static");
			if (staticPathPrefix == null) {
				staticPathPrefix = "/public";
			}
			webMetaData.setStaticPathPrefix(staticPathPrefix);
		}

		if (webMetaData.getRackApplicationPoolName() == null) {
			String beanName = AttachmentUtils.beanName(unit, RackApplicationPool.class);
			webMetaData.setRackApplicationPoolName( beanName );

		}

		return webMetaData;
	}

}
