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

import java.io.IOException;
import java.util.*;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.vdf.PojoDeployment;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.metadata.TorqueBoxYamlParser;

/**
 * <pre>
 * Stage: PARSE
 *    In: *-rack.yml
 *   Out: RackApplicationMetaData
 * </pre>
 * 
 * Creates a rack deployment from an external descriptor
 */
public class AppRackYamlParsingDeployer extends AbstractVFSParsingDeployer<RackApplicationMetaData> {

    public AppRackYamlParsingDeployer() {
        super(RackApplicationMetaData.class);
        addOutput(BeanMetaData.class);
        setSuffix("-rack.yml");
        setStage(DeploymentStages.PARSE);
    }

    @Override
    protected RackApplicationMetaData parse(VFSDeploymentUnit vfsUnit, VirtualFile file, RackApplicationMetaData root) throws Exception {
        Deployment deployment = parseAndSetUp(file);
        if (deployment == null) {
            throw new DeploymentException("Unable to parse: " + file);
        }
        attachPojoDeploymentBeanMetaData(vfsUnit, deployment);
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
        if ( rackMetaData.getRackRoot().isDirectory() ) {
            // TODO: Figure out why doing this breaks non-directory (archive) deployments.
            attachments.addAttachment(StructureMetaData.class, createStructureMetaData());
        }
        return deployment;
    }

    private StructureMetaData createStructureMetaData() {
        StructureMetaData result = StructureMetaDataFactory.createStructureMetaData();
        List<String> metaDataPaths = new ArrayList<String>();
        metaDataPaths.add("");
        metaDataPaths.add("config");
        ContextInfo context = StructureMetaDataFactory.createContextInfo("", metaDataPaths, null);
        result.addContext(context);
        return result;
    }

    private Deployment parseAndSetUp(VirtualFile file) throws IOException {
        TorqueBoxYamlParser parser = new TorqueBoxYamlParser();
        RackApplicationMetaData rackMetaData = parser.parse(file);
        log.info(rackMetaData);
        return rackMetaData==null ? null : createDeployment(rackMetaData);
    }
}
