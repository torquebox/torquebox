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

package org.torquebox.rack.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractRecognizer;
import org.torquebox.rack.metadata.RackApplicationMetaData;

public class RackApplicationRecognizer extends AbstractRecognizer {

    public static final String DEFAULT_RACKUP_PATH = "config.ru";

    public RackApplicationRecognizer() {
        addInput( RackApplicationMetaData.class );
        addOutput( RackApplicationMetaData.class );
        setRelativeOrder( 5000 );
    }

    @Override
    protected boolean isRecognized(VFSDeploymentUnit unit) {
        boolean isRecognized = hasAnyOf( unit.getRoot(), DEFAULT_RACKUP_PATH );
        log.debug(  "isRecognized?( " + unit.getRoot() + ") " + isRecognized  );
        return isRecognized;
    }

    @Override
    protected void handle(VFSDeploymentUnit unit) throws DeploymentException {
        log.info( "Recognized rack application: " + unit );
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.class );

        if (rackAppMetaData == null) {
            log.info( "Initializing rack application: " + unit );
            rackAppMetaData = new RackApplicationMetaData();
            rackAppMetaData.setRackUpScriptLocation( DEFAULT_RACKUP_PATH );
            unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );
        }
    }

}
