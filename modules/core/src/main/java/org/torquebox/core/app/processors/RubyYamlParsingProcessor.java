/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.app.processors;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.runtime.RubyRuntimeMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: RubyRuntimeMetaData
 *   Out: RubyRuntimeMetaData
 * </pre>
 * 
 * Parsing deployer for {@code ruby.yml} to specify ruby 1.8 -vs- 1.9, at least.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RubyYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public RubyYamlParsingProcessor() {
        setSectionName( "ruby" );
        setSupportsStandalone( false );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObj) throws Exception {

        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        if (runtimeMetaData == null) {
            log.debug( "Initializing ruby runtime metadata: " + unit );
            runtimeMetaData = new RubyRuntimeMetaData();
            unit.putAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY, runtimeMetaData );
        }

        Map<String, Object> config = (Map<String, Object>) dataObj;

        Object version = config.get( "version" );
        if ("1.8".equals( "" + version )) {
            runtimeMetaData.setVersion( RubyRuntimeMetaData.Version.V1_8 );
        } else if ("1.9".equals( "" + version )) {
            runtimeMetaData.setVersion( RubyRuntimeMetaData.Version.V1_9 );
        } else if ("2.0".equals( "" + version )) {
            runtimeMetaData.setVersion( RubyRuntimeMetaData.Version.V2_0 );
        }

        Object compileMode = config.get( "compile_mode" );
        if ("false".equals( "" + compileMode )) { // 'off' becomes 'false' via
                                                  // the yml parser
            runtimeMetaData.setCompileMode( RubyRuntimeMetaData.CompileMode.OFF );
        } else if ("jit".equals( "" + compileMode )) {
            runtimeMetaData.setCompileMode( RubyRuntimeMetaData.CompileMode.JIT );
        } else if ("force".equals( "" + compileMode )) {
            runtimeMetaData.setCompileMode( RubyRuntimeMetaData.CompileMode.FORCE );
        }

        Object debug = config.get( "debug" );
        if ("false".equals( "" + debug )) {
            runtimeMetaData.setDebug( false );
        } else if ("true".equals( "" + debug )) {
            runtimeMetaData.setDebug( true );
        }

        Object interactive = config.get( "interactive" );
        if ("false".equals( "" + interactive )) {
            runtimeMetaData.setInteractive( false );
        } else if ("true".equals( "" + interactive )) {
            runtimeMetaData.setInteractive( true );
        }
        
        Object profileApi = config.get( "profile_api" );
        if (profileApi != null) {
            runtimeMetaData.setProfileApi( (Boolean) profileApi );
        }

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.app.ruby" );
}
