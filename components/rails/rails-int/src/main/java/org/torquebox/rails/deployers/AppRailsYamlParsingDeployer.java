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

import java.io.IOException;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.vdf.PojoDeployment;
import org.torquebox.rack.deployers.RackStructure;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.metadata.TorqueBoxYamlParser;
import org.torquebox.rails.metadata.RailsApplicationMetaData;


/**
 * <pre>
 * Stage: PARSE
 *    In: *-rails.yml
 *   Out: RailsApplicationMetaData, RackApplicationMetaData
 * </pre>
 *
 * Creates a rails deployment from an external descriptor
 */
public class AppRailsYamlParsingDeployer extends AbstractVFSParsingDeployer<RailsApplicationMetaData> {

	public AppRailsYamlParsingDeployer() {
		super(RailsApplicationMetaData.class);
		addOutput(BeanMetaData.class);
		setSuffix("-rails.yml");
	}

	@Override
	protected RailsApplicationMetaData parse(VFSDeploymentUnit vfsUnit, VirtualFile file, RailsApplicationMetaData root) throws Exception {
        log.info("Parsing external rails descriptor: "+file);

		Deployment deployment = parseAndSetUp(vfsUnit, file);

		if (deployment != null) {
			attachPojoDeploymentBeanMetaData(vfsUnit, deployment);
		}

		// Returning null since the RailsMetaData is actually
		// attached as a predetermined managed object on the
		// sub-deployment, and not directly applicable
		// to *this* deployment unit.
		return null;

	}

	protected void attachPojoDeploymentBeanMetaData(VFSDeploymentUnit unit, Deployment deployment) {
		String beanName = AttachmentUtils.beanName(unit, PojoDeployment.class, unit.getSimpleName());

		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, PojoDeployment.class.getName());

		ValueMetaData deployerInject = builder.createInject("MainDeployer");

		builder.addConstructorParameter(DeployerClient.class.getName(), deployerInject);
		builder.addConstructorParameter(VFSDeployment.class.getName(), deployment);

		AttachmentUtils.attach(unit, builder.getBeanMetaData());
	}

	private Deployment createDeployment(RackApplicationMetaData rackMetaData) throws IOException {
		AbstractVFSDeployment deployment = new AbstractVFSDeployment(rackMetaData.getRackRoot());

		MutableAttachments attachments = ((MutableAttachments) deployment.getPredeterminedManagedObjects());

        attachments.addAttachment(RackApplicationMetaData.class, rackMetaData);
		attachments.addAttachment(RailsApplicationMetaData.class, new RailsApplicationMetaData( rackMetaData ));
		
        if ( rackMetaData.getRackRoot().isDirectory() ) {
            // TODO: Figure out why doing this breaks non-directory (archive) deployments.
            attachments.addAttachment(StructureMetaData.class, createStructureMetaData(rackMetaData.getRackRoot()));
        }

		return deployment;
	}
	
    private StructureMetaData createStructureMetaData(VirtualFile rackRoot) throws IOException {
        StructureMetaData structureMetaData = StructureMetaDataFactory.createStructureMetaData();
        ContextInfo context = RackStructure.createRackContextInfo(rackRoot, structureMetaData );
        structureMetaData.addContext(context);
        return structureMetaData;
    }


	@SuppressWarnings("unchecked")
	private Deployment parseAndSetUp(VFSDeploymentUnit unit, VirtualFile file) throws IOException {
        TorqueBoxYamlParser parser = new TorqueBoxYamlParser();
        RackApplicationMetaData rackMetaData = parser.parse(file);
        log.info(rackMetaData);
        return rackMetaData==null ? null : createDeployment(rackMetaData);
	}

}
