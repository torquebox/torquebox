/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.RubyApplicationMetaData;

/**
 * <pre>
 * Stage: PRE_REAL
 *    In: 
 *   Out: mounted virtual directories
 * </pre>
 * 
 * Ensure that directories requiring writability by packaged deployments end up
 * somewhere reasonable,
 * 
 * JBOSS_HOME/server/default/log/app.rails/ for logs
 * JBOSS_HOME/server/default/tmp/rails/app.rails/ for tmp files
 * 
 */
public class ArchiveDirectoryMounter extends AbstractDeployer {

    public ArchiveDirectoryMounter() {
        setStage( DeploymentStages.PRE_REAL );
        setInput( RubyApplicationMetaData.class );
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        if (rubyAppMetaData.isArchive()) {
            try {
                mountDir( unit, rubyAppMetaData.getRoot(), "log", System.getProperty( "jboss.server.log.dir" ) + "/" + unit.getSimpleName() );
                mountDir( unit, rubyAppMetaData.getRoot(), "tmp", System.getProperty( "jboss.server.temp.dir" ) + "/rails/" + unit.getSimpleName() );
            } catch (Exception e) {
                throw new DeploymentException( e );
            }
        }
    }

    public void undeploy(DeploymentUnit unit) {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        if (rubyAppMetaData.isArchive()) {
            close( unit, "tmp" );
            close( unit, "log" );
        }
    }

    protected void mountDir(DeploymentUnit unit, VirtualFile root, String name, String path) throws IOException {
        VirtualFile logical = root.getChild( name );
        File physical = new File( path );
        physical.mkdirs();
        Closeable mount = VFS.mountReal( physical, logical );
        unit.addAttachment( attachmentName( name ), mount, Closeable.class );
    }

    protected void close(DeploymentUnit unit, String name) {
        Closeable mount = unit.getAttachment( attachmentName( name ), Closeable.class );
        if (mount != null) {
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
