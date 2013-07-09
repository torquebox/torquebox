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

package org.torquebox.jobs;

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.projectodd.polyglot.core.util.TimeInterval;

public class ScheduledJobMetaData {
    public static final AttachmentKey<AttachmentList<ScheduledJobMetaData>> ATTACHMENTS_KEY = 
            AttachmentKey.createList( ScheduledJobMetaData.class );

    public ScheduledJobMetaData() {

    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    public void setRubyClassName(String rubyClassName) {
        this.rubyClassName = rubyClassName;
    }

    public String getRubyClassName() {
        return this.rubyClassName;
    }

    public void setRubyRequirePath(String rubyRequirePath) {
        this.rubyRequirePath = rubyRequirePath;
    }

    public String getRubyRequirePath() {
        return this.rubyRequirePath;
    }

    public void setParameters(Map<String,Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String,Object> getParameters() {
        return this.parameters;
    }
    
    public void setRubySchedulerName(String rubySchedulerName) {
        this.rubySchedulerName = rubySchedulerName;
    }

    public String getRubySchedulerName() {
        return this.rubySchedulerName;
    }

    public TimeInterval getTimeout() {
        return this.timeout;
    }

    public void setTimeout(TimeInterval timeout) {
        this.timeout = timeout;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    private String group;
    private String name;
    private String description;
    private String cronExpression;
    private TimeInterval timeout;
    private String rubyClassName;
    private String rubyRequirePath;
    private Map<String, Object> parameters;
    
    private String rubySchedulerName;
    private boolean singleton = true;
    private boolean stopped = false;

}
