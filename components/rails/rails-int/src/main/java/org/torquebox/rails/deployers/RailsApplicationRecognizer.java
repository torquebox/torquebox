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

package org.torquebox.rails.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractRecognizer;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;

public class RailsApplicationRecognizer extends AbstractRecognizer {

    public RailsApplicationRecognizer() {
        addInput( RailsApplicationMetaData.class );
        addInput( RackApplicationMetaData.class );
        addOutput( RailsApplicationMetaData.class );
        addOutput( RackApplicationMetaData.class );
        setRelativeOrder( 1000 );
    }

    @Override
    protected boolean isRecognized(VFSDeploymentUnit unit) {
        return hasAnyOf( unit.getRoot(), "config/boot.rb" );
    }

    @Override
    protected void handle(VFSDeploymentUnit unit) throws DeploymentException {
        log.info( "Recognized rails application: " + unit );
        RailsApplicationMetaData railsAppMetaData = unit.getAttachment( RailsApplicationMetaData.class );

        if (railsAppMetaData == null) {
            log.info( "Initializing rails application: " + unit );
            railsAppMetaData = new RailsApplicationMetaData();
            unit.addAttachment( RailsApplicationMetaData.class, railsAppMetaData );
        }
    }

}
