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
import java.io.File;
import java.io.Closeable;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VFS;
import org.torquebox.rack.metadata.RackApplicationMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: 
 *   Out: mounted virtual directories
 * </pre>
 * 
 * Ensure that directories requiring writability by rails packaged deployments
 * end up somewhere reasonable,
 * 
 * JBOSS_HOME/server/default/log/app.rails/ for logs
 * JBOSS_HOME/server/default/tmp/rails/app.rails/ for tmp files
 * 
 */
public class RailsArchiveDirectoryMounter extends AbstractDeployer {

    public RailsArchiveDirectoryMounter() {
        setStage(DeploymentStages.PRE_REAL);
        setInput(RackApplicationMetaData.class);
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RackApplicationMetaData metaData = unit.getAttachment(RackApplicationMetaData.class);

        if (metaData.isArchive()) {
            try {
                mountRailsDir(unit, metaData.getRackRoot(), "log", System.getProperty("jboss.server.log.dir") + "/" + unit.getSimpleName());
                mountRailsDir(unit, metaData.getRackRoot(), "tmp", System.getProperty("jboss.server.temp.dir") + "/rails/" + unit.getSimpleName());
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }
    }

    public void undeploy(DeploymentUnit unit) {
        RackApplicationMetaData metaData = unit.getAttachment(RackApplicationMetaData.class);
        
        if ( metaData.isArchive() ) {
            close(unit, "tmp");
            close(unit, "log");
        }
    }

    protected void mountRailsDir(DeploymentUnit unit, VirtualFile root, String name, String path) throws IOException {
        VirtualFile logical = root.getChild(name);
        File physical = new File(path);
        physical.mkdirs();
        Closeable mount = VFS.mountReal(physical, logical);
        log.warn("Set Rails " + name + " directory to " + physical.getCanonicalPath());
        unit.addAttachment(attachmentName(name), mount, Closeable.class);
    }

    protected void close(DeploymentUnit unit, String name) {
        Closeable mount = unit.getAttachment(attachmentName(name), Closeable.class);
        if (mount != null) {
            log.info("Closing virtual " + name + " directory for " + unit.getSimpleName());
            try {
                mount.close();
            } catch (IOException ignored) {
            }
        }
    }

    protected String attachmentName(String name) {
        return name + " dir handle";
    }
}
