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

package org.torquebox;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.projectodd.polyglot.core.util.BuildInfo;
import org.torquebox.core.util.JRubyConstants;

/**
 * Primary marker and build/version information provider.
 * 
 * @author Toby Crawley
 * @author Bob McWhirter
 */
public class TorqueBox implements TorqueBoxMBean, Service<TorqueBox> {

    /**
     * Construct.
     * 
     * @throws IOException
     *             if an error occurs while reading the underlying properties
     *             file.
     */
    public TorqueBox() throws IOException {
        this.buildInfo = new BuildInfo( "org/torquebox/torquebox.properties" );
    }

    /**
     * Retrieve the version of TorqueBox.
     * 
     * <p>
     * The version is typically a string that could be used as part of a maven
     * artifact coordinate, such as <code>1.0.1</code> or
     * <code>2.x.incremental.4</code>.
     * </p>
     */
    public String getVersion() {
        return this.buildInfo.get( "TorqueBox", "version" );
    }

    /**
     * Retrieve the git commit revision use in this build.
     */
    public String getRevision() {
        return this.buildInfo.get( "TorqueBox", "build.revision" );
    }

    /**
     * Retrieve the build number, if built by our CI server.
     */
    public String getBuildNumber() {
        return this.buildInfo.get( "TorqueBox", "build.number" );
    }

    /**
     * Retrieve the user who performed the build.
     * 
     */
    public String getBuildUser() {
        return this.buildInfo.get( "TorqueBox", "build.user" );
    }

    public List<String> getComponentNames() {
        return this.buildInfo.getComponentNames();
    }

    public Map<String, String> getComponentBuildInfo(String componentName) {
        return this.buildInfo.getComponentInfo( componentName );
    }

    @Override
    public TorqueBox getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    public void printVersionInfo(Logger log) {
        log.info( "Welcome to TorqueBox AS - http://torquebox.org/" );
        log.info( formatOutput( "version", getVersion() ) );
        String buildNo = getBuildNumber();
        if (buildNo != null && !buildNo.trim().equals( "" )) {
            log.info( formatOutput( "build", getBuildNumber() ) );
        } else if (getVersion().contains( "SNAPSHOT" )) {
            log.info( formatOutput( "build", "development (" + getBuildUser() + ")" ) );
        } else {
            log.info( formatOutput( "build", "official" ) );
        }
        log.info( formatOutput( "revision", getRevision() ) );

        List<String> otherCompoments = this.buildInfo.getComponentNames();
        otherCompoments.remove( "TorqueBox" );
        log.info( "  built with:" );
        for (String name : otherCompoments) {
            String version = this.buildInfo.get( name, "version" );
            if (version != null) {
                log.info( formatOutput( "  " + name, version ) );
            }
        }

    }

    public void verifyJRubyVersion(Logger log) {
        String jrubyVersion = this.buildInfo.get( "JRuby", "version" );
        String jarVersion = JRubyConstants.getVersion();

        if (!jarVersion.equals( jrubyVersion )) {
            log.warn( "WARNING: TorqueBox was built and tested with JRuby " + 
                      jrubyVersion + " and you are running JRuby " + 
                      jarVersion + ". You may experience unexpected results. Side effects may include: itching, sleeplessness, and irritability." );
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    private String formatOutput(String label, String value) {

        StringBuffer output = new StringBuffer( "  " );
        output.append( label );
        int length = output.length();
        if (length < 20) {
            for (int i = 0; i < 20 - length; i++) {
                output.append( '.' );
            }
        }

        output.append( ' ' );
        output.append( value );

        return output.toString();
    }

    private BuildInfo buildInfo;

}
