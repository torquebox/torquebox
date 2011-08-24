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

package org.torquebox.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class TorqueBoxYamlParsingProcessor extends AbstractParsingProcessor {
    public static final String TORQUEBOX_YAML_FILE = "torquebox.yml";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        VirtualFile file = getMetaDataFile( root, TORQUEBOX_YAML_FILE );
        
        if ( file != null ) {
            try {
                TorqueBoxMetaData metaData = parse( file );
                TorqueBoxMetaData externalMetaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
                if ( externalMetaData != null ) {
                    metaData = externalMetaData.overlayOnto( metaData );
                }
                unit.putAttachment( TorqueBoxMetaData.ATTACHMENT_KEY, metaData );
            } catch (YAMLException e) {
                throw new DeploymentUnitProcessingException( e );
            } catch (IOException e) {
                throw new DeploymentUnitProcessingException( e );
            }
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public static TorqueBoxMetaData parse(VirtualFile file) throws IOException {

        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = file.openStream();
            Map<String, Object> data = (Map<String, Object>) yaml.load( in );
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            return new TorqueBoxMetaData( data );
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core" );

}
