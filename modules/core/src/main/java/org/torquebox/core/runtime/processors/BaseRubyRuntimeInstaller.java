/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.runtime.processors;

import java.io.File;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.runtime.BaseRuntimeInitializer;
import org.torquebox.core.runtime.BundlerAwareRuntimeInitializer;
import org.torquebox.core.runtime.RubyLoadPathMetaData;
import org.torquebox.core.runtime.RubyRuntimeMetaData;
import org.torquebox.core.runtime.RuntimeInitializer;

public class BaseRubyRuntimeInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null) {
            return;
        }

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        if (runtimeMetaData != null && runtimeMetaData.getRuntimeType() != null) {
            return;
        }

        if (runtimeMetaData == null) {
            runtimeMetaData = new RubyRuntimeMetaData();
            unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );
        }

        File root = rubyAppMetaData.getRoot();
        runtimeMetaData.setBaseDir( root );
        runtimeMetaData.setEnvironment( rubyAppMetaData.getEnvironmentVariables() );
        runtimeMetaData.setRuntimeType( RubyRuntimeMetaData.RuntimeType.BARE );
        runtimeMetaData.appendLoadPath( new RubyLoadPathMetaData( root ) );
        runtimeMetaData.appendLoadPath( new RubyLoadPathMetaData( new File( root, "lib" ) ) );
        runtimeMetaData.appendLoadPath( new RubyLoadPathMetaData( new File( root, "config" ) ) );

        RuntimeInitializer initializer = null;
        File gemfile = new File( root, "Gemfile" );
        if (gemfile.exists()) {
            initializer = new BundlerAwareRuntimeInitializer( rubyAppMetaData );
        } else {
        	initializer = new BaseRuntimeInitializer( rubyAppMetaData );
        }
        runtimeMetaData.setRuntimeInitializer( initializer );

    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
    }
}
