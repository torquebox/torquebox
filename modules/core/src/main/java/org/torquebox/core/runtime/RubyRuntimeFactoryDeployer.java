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

package org.torquebox.core.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceName;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.CoreServices;

public class RubyRuntimeFactoryDeployer implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData != null && runtimeMetaData != null) {
            RubyRuntimeFactory factory = new RubyRuntimeFactory( runtimeMetaData.getRuntimeInitializer() );

            List<String> loadPaths = new ArrayList<String>();

            for (RubyLoadPathMetaData loadPath : runtimeMetaData.getLoadPaths()) {
                loadPaths.add( loadPath.getURL().toExternalForm() );
            }

            Module module = unit.getAttachment( Attachments.MODULE );

            factory.setClassLoader( module.getClassLoader() );

            factory.setServiceRegistry( phaseContext.getServiceRegistry() );
            factory.setLoadPaths( loadPaths );
            factory.setApplicationName( rubyAppMetaData.getApplicationName() );
            factory.setUseJRubyHomeEnvVar( this.useJRubyHomeEnvVar );
            factory.setApplicationEnvironment( rubyAppMetaData.getEnvironmentVariables() );
            factory.setDebug( runtimeMetaData.isDebug() );

            if (runtimeMetaData.getVersion() == RubyRuntimeMetaData.Version.V1_9) {
                factory.setRubyVersion( CompatVersion.RUBY1_9 );
            } else {
                factory.setRubyVersion( CompatVersion.RUBY1_8 );
            }

            RubyRuntimeMetaData.CompileMode compileMode = runtimeMetaData.getCompileMode();

            if (compileMode == RubyRuntimeMetaData.CompileMode.JIT) {
                factory.setCompileMode( CompileMode.JIT );
            } else if (compileMode == RubyRuntimeMetaData.CompileMode.OFF) {
                factory.setCompileMode( CompileMode.OFF );
            } else if (compileMode == RubyRuntimeMetaData.CompileMode.FORCE) {
                factory.setCompileMode( CompileMode.FORCE );
            }

            RubyRuntimeFactoryService service = new RubyRuntimeFactoryService( factory );
            ServiceName name = CoreServices.runtimeFactoryName( unit );
            
            phaseContext.getServiceTarget().addService( name, service )
                    .install();
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        log.info( "undeploy!: " + context );
    }

    private boolean useJRubyHomeEnvVar = true;

    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );

}
