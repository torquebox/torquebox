/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;
import org.torquebox.rails.core.RailsRuntimeInitializer;
import org.torquebox.rack.deployers.RackDefaultsDeployer;


/**
 * <pre>
 * Stage: POST_PARSE
 *    In: RailsApplicationMetaData, RackApplicationMetaData
 *   Out: RackApplicationMetaData
 * </pre>
 *
 * All Rails apps are essentially Rack apps, so from a Rails app we
 * construct Rack metadata to hand off to the Rack deployers.  We
 * assume that any deployer that attached a RailsApplicationMetaData
 * also attached a corresponding RackApplicationMetaData.
 */
public class RailsRackDeployer extends AbstractSimpleVFSRealDeployer<RailsApplicationMetaData> {

    public RailsRackDeployer() {
        super(RailsApplicationMetaData.class);
        addInput(RackApplicationMetaData.class);
        addInput(RackDefaultsDeployer.COMPLETE);
        addOutput(RackApplicationMetaData.class);
        setStage(DeploymentStages.POST_PARSE);
    }

    @Override
    public void deploy(VFSDeploymentUnit unit, RailsApplicationMetaData railsAppMetaData) throws DeploymentException {
        log.info(railsAppMetaData);
        try {
            RackApplicationMetaData rackMetaData = unit.getAttachment(RackApplicationMetaData.class);
            if (railsAppMetaData.isRails3()) {
                rackMetaData.setRackUpScriptLocation( rackMetaData.getRackRoot().getChild("config.ru") );
            } else {
                rackMetaData.setRackUpScript( getRackUpScript(rackMetaData.getContextPath()) );
            }
            rackMetaData.setRuntimeInitializer( new RailsRuntimeInitializer(rackMetaData.getRackRoot(), 
                                                                            rackMetaData.getRackEnv(), 
                                                                            railsAppMetaData.needsGems() ) );
            log.info(rackMetaData);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    protected String getRackUpScript(String context) {
        if (context.endsWith("/")) {
            context = context.substring(0, context.length() - 1);
        }
        return "TORQUEBOX_RACKUP_CONTEXT=%q(" + context + ")\n" + "require %q(org/torquebox/rails/deployers/rackup)\n" + "run TorqueBox::Rails.app\n";

    }

}
