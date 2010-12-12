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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.rack.metadata.RackApplicationMetaData;

/**
 * <pre>
 * Stage: POST_PARSE
 *    In: RackApplicationMetaData
 *   Out: RackApplicationMetaData, "ALL FIELDS SET"
 * </pre>
 * 
 */
public class RackDefaultsDeployer extends AbstractDeployer {

    public static final String COMPLETE = "ALL FIELDS SET";

    public RackDefaultsDeployer() {
        setStage(DeploymentStages.POST_PARSE);
        setInput(RackApplicationMetaData.class);
        addOutput(RackApplicationMetaData.class);
        addOutput(COMPLETE);
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        try {
            RackApplicationMetaData metadata = unit.getAttachment(RackApplicationMetaData.class);
            metadata.setRackEnv("development");
            metadata.setRackUpScriptLocation("config.ru");
            if (metadata.getHosts().isEmpty()) {
                metadata.addHost("localhost");
            }
            metadata.setContextPath("/");
            metadata.setStaticPathPrefix("/public");
            log.info(metadata);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
}
