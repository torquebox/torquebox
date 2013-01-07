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

package org.torquebox.core.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.analysis.Injectable;

public abstract class SimpleNamedInjectable implements Injectable {
    
    private String type;
    private String name;
    private boolean generic;
    private boolean optional;

    public SimpleNamedInjectable(String type, String name, boolean generic) {
        this( type, name, generic, false );
    }
    
    public SimpleNamedInjectable(String type, String name, boolean generic, boolean optional) {
        this.type = type;
        this.name = name;
        this.generic = generic;
        this.optional = optional;
    }
    
    public boolean isGeneric() {
        return this.generic;
    }
    
    public boolean isOptional() {
        return this.optional;
    }
    
    public String getType() {
        return this.type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getKey() {
        return getName();
    }
    
    public String toString() {
        return "[" + getClass().getName() + ": " + this.name + "]";
    }
    
    protected ServiceName wrapWithConverter(DeploymentPhaseContext context, ServiceName serviceName) {
        return serviceName;
    }


}
