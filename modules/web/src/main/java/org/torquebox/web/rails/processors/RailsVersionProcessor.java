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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.web.rails.RailsMetaData;

public class RailsVersionProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        
        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        RailsMetaData railsAppMetaData = unit.getAttachment( RailsMetaData.ATTACHMENT_KEY );
        
        if ( rubyAppMetaData == null || railsAppMetaData == null) {
            return;
        }
        
        File railsRoot = rubyAppMetaData.getRoot();
        railsAppMetaData.setVersionSpec( determineRailsVersion( railsRoot ) );
    }
    
    protected String determineRailsVersion(File railsRoot) throws DeploymentUnitProcessingException {
        String version = null;
        try {
            version = determineVersionTryRails2Vendor( railsRoot );

            if (version != null) {
                return version;
            }

            version = determineVersionTryRails2( railsRoot );

            if (version != null) {
                return version;
            }

            version = determineVersionTryRails3( railsRoot );
            return version;

        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    protected String determineVersionTryRails2Vendor(File railsRoot) throws IOException {
        File railsVersion = new File( railsRoot, "vendor/rails/railties/lib/rails/version.rb" );

        if (!railsVersion.exists()) {
            return null;
        }

        Pattern majorPattern = Pattern.compile( "^\\s*MAJOR\\s*=\\s*([0-9]+)\\s*$" );
        String major = find( railsVersion, majorPattern );

        if (major != null) {

            Pattern minorPattern = Pattern.compile( "^\\s*MINOR\\s*=\\s*([0-9]+)\\s*$" );
            String minor = find( railsVersion, minorPattern );

            if (minor != null) {

                Pattern tinyPattern = Pattern.compile( "^\\s*TINY\\s*=\\s*([^\\s]+)\\s*$" );
                String tiny = find( railsVersion, tinyPattern );

                if (tiny != null) {
                    return "" + major + "." + minor + "." + tiny;
                }
            }
        }

        return null;
    }

    protected String determineVersionTryRails2(File railsRoot) throws IOException {
        File configEnvironmentFile = new File( railsRoot, "/config/environment.rb" );
        if (configEnvironmentFile == null || !configEnvironmentFile.exists()) {
            return null;
        }
        Pattern pattern = Pattern.compile( "^[^#]*RAILS_GEM_VERSION\\s*=\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*" );
        String version = find( configEnvironmentFile, pattern );

        if (version != null) {
            return version;
        }

        return null;
    }

    protected String determineVersionTryRails3(File railsRoot) throws IOException {
        File gemfile = new File( railsRoot, "Gemfile" );
        if (gemfile == null || !gemfile.exists()) {
            return null;
        }
        Pattern pattern = Pattern.compile( "^[^#]*gem\\s*['\"]rails['\"]\\s*,\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*" );
        String version = find( gemfile, pattern );

        if (version == null) {
            version = "3.x.x.default";
        }

        return version;
    }

    protected String find(File file, Pattern pattern) throws IOException {
        BufferedReader in = null;
        try {
            InputStream inStream = new FileInputStream( file );
            InputStreamReader inReader = new InputStreamReader( inStream );
            in = new BufferedReader( inReader );
            String line = null;
            while ((line = in.readLine()) != null) {
                Matcher matcher = pattern.matcher( line );
                if (matcher.matches()) {
                    return matcher.group( 1 ).trim();
                }
            }
        } finally {
            if (in != null)
                in.close();
        }
        return null;

    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

}
