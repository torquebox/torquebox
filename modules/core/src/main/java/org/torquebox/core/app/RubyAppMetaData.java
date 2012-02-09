/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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
import org.jboss.as.server.deployment.DeploymentUnit;
import org.projectodd.polyglot.core.app.ApplicationMetaData;

public class RubyAppMetaData extends ApplicationMetaData {

    public static final AttachmentKey<RubyAppMetaData> ATTACHMENT_KEY = AttachmentKey.create( RubyAppMetaData.class );

    public RubyAppMetaData(String applicationName) {
        super( applicationName );
    }
    
    @Override
    public void attachTo(DeploymentUnit unit) {
        super.attachTo( unit );
        unit.putAttachment( ATTACHMENT_KEY, this );
    }
    
    public void extractAppEnvironment() {
        if (getEnvironmentName() == null) {
            setEnvironmentName( getAppEnvironmentFromEnvironmentVariables() );
        }        
    }
    
    @Override
    public boolean isDevelopmentMode() {
        String env = getEnvironmentName();
        if (env == null) {
            env = getAppEnvironmentFromEnvironmentVariables();
        }
        return env == null || env.trim().equalsIgnoreCase( "development" );
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
        Map<String, String> envVars = getEnvironmentVariables();
        if (envVars != null) {
            env = envVars.get( "RACK_ENV" );
            if (env == null) {
                env = envVars.get( "RAILS_ENV" );
            }
        }
        return env;
    }
    
    private Map<String, Map<String, String>> authenticationConfig;
	private Object torqueBoxInit;

	public void setTorqueBoxInit(Object torqueBoxInit) {
		this.torqueBoxInit = torqueBoxInit;		
	}
	
	public Object getTorqueBoxInit() {
		return this.torqueBoxInit;
	}
}
