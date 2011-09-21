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

package org.torquebox.core.app;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.projectodd.polyglot.core.FileLocatingProcessor;
import org.projectodd.polyglot.core.as.DeploymentNotifier;

public class RubyApplicationRecognizer extends FileLocatingProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        if (!isRubyApplication( root )) {
            return;
        }
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null) {
            rubyAppMetaData = new RubyApplicationMetaData( unit.getName() );
            rubyAppMetaData.setRoot( root );
            unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        }
        
        unit.putAttachment( DeploymentNotifier.DEPLOYMENT_TIME_ATTACHMENT_KEY, System.currentTimeMillis() );
    }

    static boolean isRubyApplication(VirtualFile file) {
        boolean result = hasAnyOf( file, 
                    "torquebox.rb", "config/torquebox.rb",
                    "torquebox.yml", "config/torquebox.yml", 
                    "config.ru", "config/environment.rb", 
                    "Rakefile", "Gemfile", ".bundle/config", "vendor/rails" );
        return result;
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core.app" );

}
