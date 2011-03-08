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
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class RubyApplicationDefaultsDeployer extends AbstractDeployer {

    public static final String DEFAULT_ENVIRONMENT_NAME = "development";

    public RubyApplicationDefaultsDeployer() {
        setStage( DeploymentStages.POST_PARSE );
        setInput( RubyApplicationMetaData.class );
        addOutput( RubyApplicationMetaData.class );
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        if (rubyAppMetaData.getEnvironmentName() == null || rubyAppMetaData.getEnvironmentName().trim().equals( "" )) {
            rubyAppMetaData.setEnvironmentName( DEFAULT_ENVIRONMENT_NAME );
        }
    }
}
