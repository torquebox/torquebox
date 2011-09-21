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

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.projectodd.polyglot.core.app.ApplicationMetaData;

public class RubyApplicationMetaData extends ApplicationMetaData {

    public static final AttachmentKey<RubyApplicationMetaData> ATTACHMENT_KEY = AttachmentKey.create( RubyApplicationMetaData.class );

    public static final String DEFAULT_ENVIRONMENT_NAME = "development";
 
    public RubyApplicationMetaData(String applicationName) {
        super( applicationName );
    }
    
    public void extractAppEnvironment() {
        if (this.environmentName == null) {
            this.environmentName = getAppEnvironmentFromEnvironmentVariables();
        }        
    }
    
    public void applyDefaults() {
        if (this.environmentName == null) {
            this.environmentName = DEFAULT_ENVIRONMENT_NAME;
        }
    }

    public boolean isDevelopmentMode() {
        String env = this.environmentName;
        if (env == null) {
            env = getAppEnvironmentFromEnvironmentVariables();
        }
        return env == null || env.trim().equalsIgnoreCase( "development" );
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getEnvironmentName() {
        return this.environmentName;
    }

    public void setEnvironmentVariables(Map<String, String> environment) {
        this.environment = environment;
    }

    public Map<String, String> getEnvironmentVariables() {
        return this.environment;
    }

    public String toString() {
        return "[RubyApplicationMetaData:\n  root=" + getRoot() + "\n  environmentName=" + this.environmentName + "\n  archive=" + isArchive() + "\n  environment="
                + this.environment + "]";
    }

    public Map<String, Map<String, String>> getAuthenticationConfig() {
        return this.authenticationConfig;
    }

    public void setAuthenticationConfig(
            Map<String, Map<String, String>> authConfig) {
        this.authenticationConfig = authConfig;
    }

    protected String getAppEnvironmentFromEnvironmentVariables() {
        String env = null;
        if (this.environment != null) {
            env = this.environment.get( "RACK_ENV" );
            if (env == null) {
                env = this.environment.get( "RAILS_ENV" );
            }
        }
        return env;
    }
    
    private String environmentName;
    private Map<String, String> environment;
    private Map<String, Map<String, String>> authenticationConfig;
}
