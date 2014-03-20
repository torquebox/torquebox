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

package org.torquebox.web.rails.processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VirtualFile;
import org.projectodd.polyglot.core.processors.FileLocatingProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.web.rack.RackMetaData;
import org.torquebox.web.rails.RailsMetaData;

public class RailsApplicationRecognizer extends FileLocatingProcessor {

    private static final String RAILS3_FILE = "config/application.rb";
    private static final Pattern RAILS3_PATTERN = Pattern.compile( "^.*Rails\\:\\:Application.*$" );

    private static final String RAILS2_FILE = "config/environment.rb";
    private static final Pattern RAILS2_PATTERN = Pattern.compile( "^.*Rails\\:\\:Initializer.*$" );

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        try {
            if (isRailsApplication( root )) {
                RackMetaData rackAppMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );
                RailsMetaData railsAppMetaData = unit.getAttachment( RailsMetaData.ATTACHMENT_KEY );

                if (rackAppMetaData == null) {
                    rackAppMetaData = new RackMetaData();
                    rackAppMetaData.attachTo( unit );
                }

                if (railsAppMetaData == null) {
                    railsAppMetaData = new RailsMetaData();
                    unit.putAttachment( RailsMetaData.ATTACHMENT_KEY, railsAppMetaData );
                }
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException("Error processing rails file: ", e);
        }

    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    /**
     * Checks to see if the deployment is a Rails application. It does so by
     * looking for either config/application.rb (Rails 3) or
     * config/environment.rb
     * (Rails 2), and checking for Rails content within them.
     * 
     * @param root The root VFS location for the app
     * @return True if the app is a Rails app, false otherwise.
     */
    static boolean isRailsApplication(VirtualFile root) throws IOException {
        return findRailsPattern( root, RAILS3_FILE, RAILS3_PATTERN ) || findRailsPattern( root, RAILS2_FILE, RAILS2_PATTERN );
    }

    private static boolean findRailsPattern(VirtualFile root, String fileName, Pattern pattern) throws IOException {
        boolean retValue = false;
        VirtualFile railsFile = root.getChild( fileName );
        if (railsFile.exists()) {
            BufferedReader brIn = new BufferedReader( new InputStreamReader( railsFile.openStream() ) );
            String line;
            while ((line = brIn.readLine()) != null) {
                if (pattern.matcher( line ).matches()) {
                    retValue = true;
                    break;
                }
            }
            brIn.close();
        }
        return retValue;
    }

}
