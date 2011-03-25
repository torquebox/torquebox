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

import java.net.MalformedURLException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;

public class BaseRubyRuntimeDeployer extends AbstractDeployer {

    public BaseRubyRuntimeDeployer() {
        setInput(RubyApplicationMetaData.class);
        addInput(RubyRuntimeMetaData.class);
        setStage( DeploymentStages.PRE_DESCRIBE );
        setRelativeOrder( 10000 );
    }
    
    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment(  RubyRuntimeMetaData.class );
        
        if ( runtimeMetaData != null && runtimeMetaData.getRuntimeType() != null ) {
            log.warn( "Ruby runtime already configured as " + runtimeMetaData.getRuntimeType() + ": " + unit );
            return;
        }
        
        log.debug("Deploying base ruby runtime: " + unit );
        
        if (runtimeMetaData == null) {
            runtimeMetaData = new RubyRuntimeMetaData();
            unit.addAttachment(  RubyRuntimeMetaData.class, runtimeMetaData );
        }

        RubyApplicationMetaData appMetaData = unit.getAttachment(  RubyApplicationMetaData.class  );

        runtimeMetaData.setRuntimeType( RubyRuntimeMetaData.RuntimeType.BARE );
        runtimeMetaData.setBaseDir( appMetaData.getRoot() );
        runtimeMetaData.setEnvironment(  appMetaData.getEnvironmentVariables()  );

        try {
            runtimeMetaData.appendLoadPath( new RubyLoadPathMetaData( appMetaData.getRoot().toURL() ) );
        } catch (MalformedURLException e) {
            throw new DeploymentException( e );
        }
        

    }

}
