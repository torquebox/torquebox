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
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.TorqueBoxMetaData;

public class MergedTorqueBoxMetaDataDeployer extends AbstractDeployer {

    public MergedTorqueBoxMetaDataDeployer() {
        addInput( TorqueBoxMetaData.class );
        addInput( TorqueBoxMetaData.EXTERNAL );
        addOutput( TorqueBoxMetaData.class );
        setAllInputs( true );
        setStage( DeploymentStages.PARSE );
        setRelativeOrder( -1000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        TorqueBoxMetaData externalMetaData = unit.getAttachment( TorqueBoxMetaData.EXTERNAL, TorqueBoxMetaData.class );
        if (log.isTraceEnabled()) {
            log.trace( "External: " + externalMetaData );
        }

        if (externalMetaData == null) {
            return;
        }

        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.class );
        if (log.isTraceEnabled()) {
            log.trace( "Internal: " + metaData );
        }

        if (metaData == null) {
            unit.addAttachment( TorqueBoxMetaData.class, externalMetaData );
            return;
        }
        try {
            TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto( metaData );
            if (log.isTraceEnabled()) {
                log.trace( "Merged: " + mergedMetaData );
            }
            unit.addAttachment( TorqueBoxMetaData.class, mergedMetaData );
        } catch (ClassCastException e) {
            throw new DeploymentException("Incompatible structures in external and internal deployment descriptors", e);
        }

    }
}
