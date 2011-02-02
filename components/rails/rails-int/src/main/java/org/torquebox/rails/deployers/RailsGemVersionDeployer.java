/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

    //private static final Logger log = Logger.getLogger(RailsGemVersionDeployer.class);

    public RailsGemVersionDeployer() {
        setInput(RailsApplicationMetaData.class);
        addRequiredInput(RubyApplicationMetaData.class);
        setOutput(RailsApplicationMetaData.class);
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment(RubyApplicationMetaData.class);
        RailsApplicationMetaData railsMetaData = unit.getAttachment(RailsApplicationMetaData.class);
        VirtualFile railsRoot = rubyAppMetaData.getRoot();
        
        log.info( "Rails Root = " + railsRoot );

        VirtualFile vendorRails = railsRoot.getChild("vendor/rails");
        if (vendorRails != null && vendorRails.exists()) {
            railsMetaData.setFrozen(true);
        } else {
            railsMetaData.setVersionSpec( determineRailsGemVersion(railsRoot) );
        }
    }

    protected String determineRailsGemVersion(VirtualFile railsRoot) throws DeploymentException {
        try {
            String version = determineVersionTryRails2( railsRoot );
            if ( version == null ) {
                version = determineVersionTryRails3( railsRoot );
            }
            return version;
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }
    
    protected String determineVersionTryRails2(VirtualFile railsRoot) throws IOException {
        VirtualFile configEnvironmentFile = railsRoot.getChild("/config/environment.rb");
        if (configEnvironmentFile == null || !configEnvironmentFile.exists()) {
            return null;
        }
        log.info( "config/environment.rb = " + configEnvironmentFile );
        Pattern pattern = Pattern.compile("^[^#]*RAILS_GEM_VERSION\\s*=\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*");
        return find(configEnvironmentFile, pattern);
    }
    
    protected String determineVersionTryRails3(VirtualFile railsRoot) throws IOException {
        VirtualFile gemfile = railsRoot.getChild("Gemfile");
        if (gemfile == null || !gemfile.exists()) {
            return null;
        }
        log.info( "Gemfile = " + gemfile );
        Pattern pattern = Pattern.compile("^[^#]*gem\\s*['\"]rails['\"]\\s*,\\s*[\"']([!~<>=]*\\s*[\\d.]+)[\"'].*");
        return find(gemfile, pattern);
    }

    protected String find(VirtualFile file, Pattern pattern) throws IOException {
        BufferedReader in = null;
        try {
            InputStream inStream = file.openStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            in = new BufferedReader(inReader);
            String line = null;
            while ((line = in.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    return matcher.group(1).trim();
                }
            }
        } finally {
            if (in != null) in.close();
        }
        return null;

    }
}
