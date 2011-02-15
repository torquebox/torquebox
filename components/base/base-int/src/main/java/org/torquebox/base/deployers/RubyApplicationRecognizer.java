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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class RubyApplicationRecognizer extends AbstractRecognizer {

    public RubyApplicationRecognizer() {
        setStage( DeploymentStages.PRE_PARSE );
        addOutput( RubyApplicationMetaData.class );
        setRelativeOrder( 5000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy( (VFSDeploymentUnit) unit );
        } else {
            log.debug( "Not a VFS deployment unit: " + unit );
        }
    }

    @Override
    protected void handle(VFSDeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        if (rubyAppMetaData == null) {
            log.debug( "Initializing ruby application: " + unit );
            rubyAppMetaData = new RubyApplicationMetaData();
            rubyAppMetaData.setRoot( unit.getRoot() );

            unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );
        } else {
            log.debug( "Ruby application already initialized: " + unit );
        }
        
        rubyAppMetaData.setApplicationName( unit.getSimpleName() );
    }

    static boolean isRubyApplication(VirtualFile file) {
        boolean result = hasAnyOf( file, "torquebox.yml", "config/torquebox.yml", "config.ru", "config/environment.rb", "Rakefile", "Gemfile", ".bundle/config", "vendor/rails" );
        return result;
    }

    protected boolean isRecognized(VFSDeploymentUnit unit) {
        return RubyApplicationRecognizer.isRubyApplication( unit.getRoot() );
    }

    protected static boolean hasAnyOf(VirtualFile root, String... paths) {
        for (String path : paths) {
            if (root.getChild( path ).exists()) {
                return true;
            }
        }
        return false;
    }

}
