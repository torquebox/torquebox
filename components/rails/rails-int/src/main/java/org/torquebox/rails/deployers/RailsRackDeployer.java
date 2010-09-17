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
import org.torquebox.rails.metadata.RailsGemVersionMetaData;

public class RailsRackDeployer extends AbstractSimpleVFSRealDeployer<RailsApplicationMetaData> {

    // private static final Logger log =
    // Logger.getLogger(RailsRackDeployer.class);

    public RailsRackDeployer() {
        super(RailsApplicationMetaData.class);
        addInput(RackApplicationMetaData.class);
        addInput(RailsGemVersionMetaData.class);
        addOutput(RackApplicationMetaData.class);
        setStage(DeploymentStages.POST_PARSE);
    }

    @Override
    public void deploy(VFSDeploymentUnit unit, RailsApplicationMetaData railsAppMetaData) throws DeploymentException {

        log.info("deploy(" + unit + ")");
        RackApplicationMetaData rackMetaData = unit.getAttachment(RackApplicationMetaData.class);

        if (rackMetaData == null) {
            rackMetaData = new RackApplicationMetaData();
            rackMetaData.setContextPath("/");
            unit.addAttachment(RackApplicationMetaData.class, rackMetaData);
        }

        rackMetaData.setStaticPathPrefix("/public");
        rackMetaData.setRackRoot(railsAppMetaData.getRailsRoot());
        rackMetaData.setRackEnv(railsAppMetaData.getRailsEnv());

        RailsGemVersionMetaData railsVersionMetaData = unit.getAttachment(RailsGemVersionMetaData.class);

        String rackUpScript = null;

        if (railsVersionMetaData.isRails3()) {
            VirtualFile configRu = railsAppMetaData.getRailsRoot().getChild("config.ru");
            rackMetaData.setRackUpScriptLocation(configRu);
            try {
                rackUpScript = read(configRu);
            } catch (IOException e) {
                throw new DeploymentException( e );
            }
        } else {
            rackUpScript = getRackUpScript(rackMetaData.getContextPath());
        }
        rackMetaData.setRackUpScript(rackUpScript);

        unit.addAttachment(RackApplicationMetaData.class, rackMetaData);
    }

    protected String getRackUpScript(String context) {
        if (context.endsWith("/")) {
            context = context.substring(0, context.length() - 1);
        }
        return "TORQUEBOX_RACKUP_CONTEXT=%q(" + context + ")\n" + "require %q(org/torquebox/rails/deployers/rackup)\n" + "run TorqueBox::Rails.app\n";

    }

    private String read(VirtualFile file) throws IOException {
        StringBuilder contents = new StringBuilder();
        InputStream in = file.openStream();
        Reader reader = new InputStreamReader(in);

        try {
            char[] buf = new char[1024];

            while (reader.read(buf) >= 0 ) {
                contents.append(buf);
            }

            return contents.toString();
        } finally {
            reader.close();
        }
    }

}
