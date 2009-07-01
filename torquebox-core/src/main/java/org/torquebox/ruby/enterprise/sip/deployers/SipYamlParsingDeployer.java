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
package org.torquebox.ruby.enterprise.sip.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jruby.util.ByteList;
import org.jvyamlb.YAML;
import org.torquebox.ruby.enterprise.sip.metadata.SipApplicationMetaData;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class SipYamlParsingDeployer extends AbstractVFSParsingDeployer<SipApplicationMetaData> {

	/**
	 * @param output
	 */
	public SipYamlParsingDeployer() {
		super(SipApplicationMetaData.class);
		setName("sip.yml");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer#parse
	 * (org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit,
	 * org.jboss.virtual.VirtualFile, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected SipApplicationMetaData parse(VFSDeploymentUnit unit, VirtualFile file, SipApplicationMetaData arg2)
			throws Exception {
		Map<ByteList, ByteList> sip = (Map<ByteList, ByteList>) YAML.load(file.openStream());

		ByteList rubyController = sip.get( ByteList.create( "rubycontroller") );

		SipApplicationMetaData sipMetaData = unit.getAttachment(SipApplicationMetaData.class);

		if (sipMetaData == null) {
			sipMetaData = new SipApplicationMetaData();
			unit.addAttachment(SipApplicationMetaData.class, sipMetaData);
		}

		if (sipMetaData.getRubyController() == null) {
			if ( rubyController != null ) {
				sipMetaData.setRubyController(rubyController.toString());
			}
		}

		return sipMetaData;
	}

}
