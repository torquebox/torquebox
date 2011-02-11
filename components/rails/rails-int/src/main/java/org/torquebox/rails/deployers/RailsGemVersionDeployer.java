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

package org.torquebox.rails.deployers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: RailsApplicationMetaData
 *   Out: RailsApplicationMetaData
 * </pre>
 * 
 * Determine which rails version the deployment requires
 */
public class RailsGemVersionDeployer extends AbstractParsingDeployer {

    // private static final Logger log =
    // Logger.getLogger(RailsGemVersionDeployer.class);

    public RailsGemVersionDeployer() {
        setInput( RailsApplicationMetaData.class );
        addRequiredInput( RubyApplicationMetaData.class );
        setOutput( RailsApplicationMetaData.class );
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        RailsApplicationMetaData railsMetaData = unit.getAttachment( RailsApplicationMetaData.class );
        VirtualFile railsRoot = rubyAppMetaData.getRoot();

        log.info( "Rails Root = " + railsRoot );

        railsMetaData.setVersionSpec( determineRailsGemVersion( railsRoot ) );
    }

    protected String determineRailsGemVersion(VirtualFile railsRoot) throws DeploymentException {
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
            throw new DeploymentException( e );
        }
    }

    protected String determineVersionTryRails2Vendor(VirtualFile railsRoot) throws IOException {
        log.debug( "Try vendored rails 2: " + railsRoot );
        VirtualFile railsVersion = railsRoot.getChild( "vendor/rails/railties/lib/rails/version.rb" );

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
                    log.debug( "Matched vendored rails 2: " + railsRoot );
                    return "" + major + "." + minor + "." + tiny;
                }
            }
        }

        return null;
    }

    protected String determineVersionTryRails2(VirtualFile railsRoot) throws IOException {
        log.debug( "Try gems rails 2: " + railsRoot );
        VirtualFile configEnvironmentFile = railsRoot.getChild( "/config/environment.rb" );
        if (configEnvironmentFile == null || !configEnvironmentFile.exists()) {
            return null;
        }
        log.info( "config/environment.rb = " + configEnvironmentFile );
        Pattern pattern = Pattern.compile( "^[^#]*RAILS_GEM_VERSION\\s*=\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*" );
        String version = find( configEnvironmentFile, pattern );

        if (version != null) {
            log.debug( "Matched gems rails 2: " + railsRoot );
            return version;
        }

        return null;
    }

    protected String determineVersionTryRails3(VirtualFile railsRoot) throws IOException {
        log.debug( "Try rails 3: " + railsRoot );
        VirtualFile gemfile = railsRoot.getChild( "Gemfile" );
        if (gemfile == null || !gemfile.exists()) {
            return null;
        }
        log.info( "Gemfile = " + gemfile );
        Pattern pattern = Pattern.compile( "^[^#]*gem\\s*['\"]rails['\"]\\s*,\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*" );
        String version = find( gemfile, pattern );

        if (version == null) {
            version = "3.x.x.default";
        }

        log.debug( "Matched rails 3: " + railsRoot );

        return version;
    }

    protected String find(VirtualFile file, Pattern pattern) throws IOException {
        BufferedReader in = null;
        try {
            InputStream inStream = file.openStream();
            InputStreamReader inReader = new InputStreamReader( inStream );
            in = new BufferedReader( inReader );
            String line = null;
            while ((line = in.readLine()) != null) {
                log.debug( "line: " + line );
                Matcher matcher = pattern.matcher( line );
                if (matcher.matches()) {
                    log.debug( "MATCH: " + matcher.group( 1 ) );
                    return matcher.group( 1 ).trim();
                }
            }
        } finally {
            if (in != null)
                in.close();
        }
        return null;

    }
}
