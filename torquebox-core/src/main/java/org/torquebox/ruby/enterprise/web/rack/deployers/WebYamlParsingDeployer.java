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
package org.torquebox.ruby.enterprise.web.rack.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jruby.util.ByteList;
import org.jvyamlb.YAML;
import org.torquebox.ruby.enterprise.web.rack.metadata.RackWebApplicationMetaData;

public class WebYamlParsingDeployer extends AbstractVFSParsingDeployer<RackWebApplicationMetaData> {

	public WebYamlParsingDeployer() {
		super(RackWebApplicationMetaData.class);
		setName("web.yml");
	}

	@SuppressWarnings("unchecked")
	protected RackWebApplicationMetaData parse(VFSDeploymentUnit unit, VirtualFile file, RackWebApplicationMetaData root)
			throws Exception {
		Map<ByteList, ByteList> web = (Map<ByteList, ByteList>) YAML.load(file.openStream());

		ByteList contextBytes = web.get(ByteList.create("context"));
		String context = null;

		if (contextBytes == null) {
			context = "/";
		}

		ByteList hostBytes = web.get(ByteList.create("host"));
		String host = null;

		if (hostBytes == null) {
			host = "localhost";
		} else {
			host = hostBytes.toString().trim();
			if (host.equals("")) {
				host = "localhost";
			}
		}

		RackWebApplicationMetaData webMetaData = unit.getAttachment(RackWebApplicationMetaData.class);

		if (webMetaData == null) {
			webMetaData = new RackWebApplicationMetaData();
			unit.addAttachment(RackWebApplicationMetaData.class, webMetaData);
		}

		if (webMetaData.getContext() == null) {
			webMetaData.setContext(context);
		}

		if (webMetaData.getHost() == null) {
			webMetaData.setHost(host);
		}

		return webMetaData;
	}

}
