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

package org.torquebox.interp.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.Kernel;

import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.common.spi.DeferrablePool;

/**
 * This deployer 'warms up' any deferred runtime pools by filling them before they are needed. It
 * adds complexity, however, and will warm up pools that may never be used (messaging for Backgroundable
 * when Backgroundable isn't used anywhere comes to mind). I'm commenting it out of 
 * torquebox-base-int-jboss-beans.xml for now until we decide what to do with it (see TORQUE-383).
 */
public class DeferredPoolInitializer extends AbstractDeployer {

    public DeferredPoolInitializer() {
        setStage( DeploymentStages.INSTALLED );
        setRelativeOrder( 1000 );
    }

    @Override
    public void deploy(final DeploymentUnit unit) throws DeploymentException {
        Set<? extends PoolMetaData> metaData = unit.getAllMetaData( PoolMetaData.class );

        for (PoolMetaData each : metaData) {
            String beanName = AttachmentUtils.beanName( unit, RubyRuntimePool.class, each.getName() );
            ControllerContext entry = this.kernel.getController().getContext( beanName, ControllerState.START );
            if (entry != null) {
                Object target = entry.getTarget();
                if (target instanceof DeferrablePool) {
                    final DeferrablePool pool = (DeferrablePool)target;
                    if (pool.isDeferred()) {
                        (new Thread() {
                                public void run() {
                                    try {
                                        pool.startPool( false );
                                    } catch(Exception ex) {
                                        log.error( "Failed to start pool", ex );
                                    }
                                }
                            }).start();
                    }
                }
            }
        }
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public Kernel getKernel() {
        return this.kernel;
    }

    private Kernel kernel;
}
