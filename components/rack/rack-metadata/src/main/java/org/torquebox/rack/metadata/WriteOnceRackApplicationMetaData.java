/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.torquebox.rack.metadata;

import java.util.Map;
import org.jboss.vfs.VirtualFile;

/**
 * This is a write-once bean, in which setters only take effect if
 * their corresponding getter returns null.  Setters taking a Map only
 * write those entries whose keys aren't already in the Map.
 *
 * This reflects the idea that higher-priority deployers, e.g. those
 * reading external descriptors, manipulate the meta data *before*
 * lower-priority ones, e.g. those reading internal descriptors.
 *
 * This would be SO MUCH EASIER (and smaller, and more robust,
 * resilient to changes in the baseclass) in a dynamic language!
 */
public class WriteOnceRackApplicationMetaData extends RackApplicationMetaData {
	
	public void setRackRoot(VirtualFile rackRoot) {
		if (null == getRackRoot()) super.setRackRoot( rackRoot );
	}
	
	public void setRackEnv(String rackEnv) {
		if (null == getRackEnv()) super.setRackEnv( rackEnv );
	}
	
	public void setRackUpScript(String rackUpScript) {
		if (null == getRackUpScript()) super.setRackUpScript( rackUpScript );
	}

	public void setRackUpScriptLocation(VirtualFile rackUpScriptLocation) {
		if (null == getRackUpScriptLocation()) super.setRackUpScriptLocation( rackUpScriptLocation );
	}
	
	public void setContextPath(String contextPath) {
		if (null == getContextPath()) super.setContextPath( contextPath );
	}
	
	public void setStaticPathPrefix(String staticPathPrefix) {
		if (null == getStaticPathPrefix()) super.setStaticPathPrefix( staticPathPrefix );
	}
	
	public void setRubyRuntimePoolName(String rubyRuntimePoolName) {
		if (null == getRubyRuntimePoolName()) super.setRubyRuntimePoolName( rubyRuntimePoolName );
	}
	
	public void setRackApplicationFactoryName(String rackApplicationFactoryName) {
		if (null == getRackApplicationFactoryName()) super.setRackApplicationFactoryName( rackApplicationFactoryName );
	}
	
	public void setRackApplicationPoolName(String rackApplicationPoolName) {
		if (null == getRackApplicationPoolName()) super.setRackApplicationPoolName( rackApplicationPoolName );
	}
	
	public void setEnvironmentVariables(Map<String,String> environment) {
		if (null == getEnvironmentVariables()) { 
            super.setEnvironmentVariables( environment );
        } else {
            Map<String,String> current = getEnvironmentVariables();
            for (String key: environment.keySet()) {
                if ( ! current.containsKey(key) ) {
                    current.put( key, environment.get(key) );
                }
            }
        }
	}

}
