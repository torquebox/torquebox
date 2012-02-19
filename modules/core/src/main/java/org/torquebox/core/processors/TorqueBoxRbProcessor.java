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

package org.torquebox.core.processors;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.runtime.builtin.IRubyObject;
import org.projectodd.polyglot.core.processors.AbstractParsingProcessor;
import org.torquebox.core.GlobalRuby;
import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.core.as.CoreServices;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TorqueBoxRbProcessor extends AbstractParsingProcessor {

    public static final String TORQUEBOX_RB_FILE = "torquebox.rb";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        VirtualFile vFile = getMetaDataFile( root, TORQUEBOX_RB_FILE );

        
        if ( vFile != null ) {
            HashMap<String, Object> metaDataHash;
            File file;
            
            try {
                file = vFile.getPhysicalFile();
            } catch (IOException e) {
                log.error( e );
                return;
            }
            
            try {
                GlobalRuby ruby = (GlobalRuby)phaseContext.getServiceRegistry().getRequiredService( CoreServices.GLOBAL_RUBY ).getValue();
                metaDataHash =  eval( ruby, file );
            } catch (Exception e) {
                log.error( "============================================================" );
                log.error( "Failed to load '" + file.getAbsolutePath() + "':"  );
                log.error( "  " + e.getMessage() );
                log.error( "============================================================" );
                
                throw new DeploymentUnitProcessingException( "Failed to load " + file.getAbsolutePath(), e );
            }
            TorqueBoxMetaData metaData = new TorqueBoxMetaData( metaDataHash );
            TorqueBoxMetaData existingMetaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
            if ( existingMetaData != null ) {
                metaData = existingMetaData.overlayOnto( metaData );
            }
            unit.putAttachment( TorqueBoxMetaData.ATTACHMENT_KEY, metaData );
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> eval(GlobalRuby ruby, File file) throws Exception {
        log.info( "evaling: " + file );

        StringBuffer script = new StringBuffer( "require 'rubygems'\n");
        script.append( "require 'torquebox-configure'\n" );
        script.append( "TorqueBox::Configuration::GlobalConfiguration.load_configuration( %q{" ).append( file.getAbsolutePath() ).append( "} ).to_java" );

        return (HashMap<String, Object>)((IRubyObject)ruby.evaluate( script.toString() )).toJava( HashMap.class );
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core" );

}
