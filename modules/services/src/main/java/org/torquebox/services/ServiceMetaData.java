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

package org.torquebox.services;

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class ServiceMetaData {

    public static final AttachmentKey<AttachmentList<ServiceMetaData>> ATTACHMENTS_KEY = AttachmentKey.createList(ServiceMetaData.class);

    public ServiceMetaData() {

    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setParameters(Map<String,Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String,Object> getParameters() {
        return this.parameters;
    }

    public void setRequiresSingleton(boolean requiresSingleton) {
        this.requiresSingleton = requiresSingleton;
    }

    public boolean isRequiresSingleton() {
        return this.requiresSingleton;
    }

    public void setRubyRequirePath(String rubyRequirePath) {
        this.rubyRequirePath = rubyRequirePath;
    }

    public String getRubyRequirePath() {
        return this.rubyRequirePath;
    }

    private String className;
    private String rubyRequirePath;
    private String name;
    private Map<String, Object> parameters;
    private boolean requiresSingleton = true;

}
