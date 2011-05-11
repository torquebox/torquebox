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

package org.torquebox.web.rack;

import java.util.Collections;
import java.util.List;

import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.web.deployment.TldsMetaData;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.spec.TldMetaData;
import org.jboss.vfs.VirtualFile;

public class RackApplicationRecognizer implements DeploymentUnitProcessor {

    public static final String DEFAULT_RACKUP_PATH = "config.ru";

    public RackApplicationRecognizer() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        if (isRubyApplication( root )) {
            RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

            if (rackAppMetaData == null) {
                rackAppMetaData = new RackApplicationMetaData();
                rackAppMetaData.setRackUpScriptLocation( DEFAULT_RACKUP_PATH );
                unit.putAttachment( RackApplicationMetaData.ATTACHMENT_KEY, rackAppMetaData );
            }
            log.info( "Marking as WAR" );
            DeploymentTypeMarker.setType( DeploymentType.WAR, unit );
            WarMetaData warMetaData = new WarMetaData();

            final TldsMetaData tldsMetaData = new TldsMetaData();
            List<TldMetaData> sharedTldsMetaData = Collections.emptyList();
            tldsMetaData.setSharedTlds( sharedTldsMetaData );
            unit.putAttachment( TldsMetaData.ATTACHMENT_KEY, tldsMetaData );
            unit.putAttachment( WarMetaData.ATTACHMENT_KEY, warMetaData );
            unit.addToAttachmentList( Attachments.RESOURCE_ROOTS, resourceRoot );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    protected static boolean hasAnyOf(VirtualFile root, String... paths) {
        for (String path : paths) {
            if (root.getChild( path ).exists()) {
                return true;
            }
        }
        return false;
    }

    static boolean isRubyApplication(VirtualFile file) {
        boolean result = hasAnyOf( file, DEFAULT_RACKUP_PATH );
        return result;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack" );
}
