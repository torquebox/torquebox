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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.metadata.TorqueBoxYamlParser;


/**
 * <pre>
 * Stage: PARSE
 *    In: torquebox.yml
 *   Out: RackApplicationMetaData
 * </pre>
 *
 * Internal deployment descriptor for setting application, web, and
 * environment configuration
 */
public class TorqueBoxYamlParsingDeployer extends AbstractVFSParsingDeployer<RackApplicationMetaData> {

	public TorqueBoxYamlParsingDeployer() {
		super(RackApplicationMetaData.class);
		setName("torquebox.yml");
	}

    // Without this, the torquebox.yml won't be considered metadata
    // for a parent unit, and no, I'm not entirely sure what that
    // means.
    protected boolean allowsReparse()
    {
        return true;
    }

	protected RackApplicationMetaData parse(VFSDeploymentUnit unit, VirtualFile file, RackApplicationMetaData root) throws Exception {
        log.info("Parsing metadata: " + file);
        TorqueBoxYamlParser parser = new TorqueBoxYamlParser(root, unit.getRoot());
        RackApplicationMetaData result = parser.parse(file);
		if (result == null) {
			throw new DeploymentException("Unable to parse: " + file);
		}
		return result;
	}

}
