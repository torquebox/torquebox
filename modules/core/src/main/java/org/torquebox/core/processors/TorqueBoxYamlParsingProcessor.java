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

package org.torquebox.core.processors;

import java.util.Map;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.projectodd.polyglot.core.processors.AbstractParsingProcessor;
import org.projectodd.yaml.SchemaException;
import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.core.util.YAMLUtils;

public class TorqueBoxYamlParsingProcessor extends AbstractParsingProcessor {

    public static final String TORQUEBOX_YAML_FILE = "torquebox.yml";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        VirtualFile file = getMetaDataFile( root, TORQUEBOX_YAML_FILE );

        if (file != null) {
            Map<String, Object> data = null;
            try {
                data = YAMLUtils.parseYaml( file );
            } catch (Exception e) {
                throw new DeploymentUnitProcessingException("Error processing yaml: ", e);
            } 
                        
            TorqueBoxMetaData metaData = new TorqueBoxMetaData( data );
            TorqueBoxMetaData externalMetaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
            if (externalMetaData != null) {
                metaData = externalMetaData.overlayOnto( metaData );
            }
            
            try {
                metaData.validate();
            } catch (SchemaException e) {
                throw new DeploymentUnitProcessingException("Configuration validation failed: ", e);
            }
            unit.putAttachment( TorqueBoxMetaData.ATTACHMENT_KEY, metaData );
        }

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core" );

}
