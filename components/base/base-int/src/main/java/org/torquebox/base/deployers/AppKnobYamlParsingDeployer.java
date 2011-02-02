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
package org.torquebox.base.deployers;

import java.io.IOException;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.plugins.client.AbstractVFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.TorqueBoxMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.vdf.PojoDeployment;

/**
 * <pre>
 * Stage: PARSE
 *    In: *-knob.yml
 *   Out: BeanMetaData<PojoDeployment>
 * </pre>
 * 
 * Creates a rack deployment from an external descriptor
 */
public class AppKnobYamlParsingDeployer extends AbstractDeployer {

    public AppKnobYamlParsingDeployer() {
        addOutput(BeanMetaData.class);
        setStage(DeploymentStages.PARSE);
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy((VFSDeploymentUnit) unit);
        }
    }

    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        String name = unit.getRoot().getName();

        if (!name.endsWith("-knob.yml")) {
            return;
        }

        try {
            TorqueBoxMetaData metaData = TorqueBoxYamlParsingDeployer.parse(unit.getRoot());
            VirtualFile root = metaData.getApplicationRootFile();

            if (root == null) {
                throw new DeploymentException("No application root specified");
            }
            
            Deployment deployment = createDeployment( metaData );
            attachPojoDeploymentBeanMetaData(unit, deployment);
            
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    private Deployment createDeployment(TorqueBoxMetaData metaData) throws IOException {
        AbstractVFSDeployment deployment = new AbstractVFSDeployment(metaData.getApplicationRootFile());
        MutableAttachments attachments = ((MutableAttachments) deployment.getPredeterminedManagedObjects());
        attachments.addAttachment(TorqueBoxMetaData.class.getName() + "$external", metaData, TorqueBoxMetaData.class );
        return deployment;
    }
    
    protected void attachPojoDeploymentBeanMetaData(VFSDeploymentUnit unit, Deployment deployment) {
        String beanName = AttachmentUtils.beanName(unit, PojoDeployment.class, unit.getSimpleName());

        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(beanName, PojoDeployment.class.getName());
        ValueMetaData deployerInject = builder.createInject("MainDeployer");
        builder.addConstructorParameter(DeployerClient.class.getName(), deployerInject);
        builder.addConstructorParameter(VFSDeployment.class.getName(), deployment);

        AttachmentUtils.attach(unit, builder.getBeanMetaData());
    }

}
