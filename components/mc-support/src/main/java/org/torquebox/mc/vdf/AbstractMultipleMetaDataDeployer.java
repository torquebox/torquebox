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

package org.torquebox.mc.vdf;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

public abstract class AbstractMultipleMetaDataDeployer<T> extends AbstractDeployer {

    private Class<T> metaDataClass;

    public AbstractMultipleMetaDataDeployer(Class<T> metaDataClass) {
        addInput( metaDataClass );
        this.metaDataClass = metaDataClass;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends T> metaData = unit.getAllMetaData( this.metaDataClass );

        for (T each : metaData) {
            deploy( unit, each );
        }

    }

    protected abstract void deploy(DeploymentUnit unit, T metaData) throws DeploymentException;

}
