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

package org.torquebox.messaging.tasks;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.torquebox.core.util.StringUtils;

public class TaskMetaData {
    
    public static AttachmentKey<AttachmentList<TaskMetaData>> ATTACHMENTS_KEY = AttachmentKey.createList( TaskMetaData.class );

    public TaskMetaData() {
    
    }

    public String getName() {
        return this.location;
    }

    public void setRubyClassName(String rubyClassName) {
        this.rubyClassName = rubyClassName;
    }

    public String getRubyClassName() {
        return this.rubyClassName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public void setQueueSuffix(String suffix) {
        this.queueSuffix = suffix;
    }

    public String getQueueSuffix() {
        if (this.queueSuffix == null) {
            String baseQueueName = this.getRubyClassName();
            if (baseQueueName.endsWith( "Task" )) {
                baseQueueName = baseQueueName.substring( 0, baseQueueName.length() - 4 );
            }
            baseQueueName = StringUtils.underscore( baseQueueName );
            this.queueSuffix = baseQueueName;
        }
        return this.queueSuffix;
    }

    public void setConcurrency(Integer concurrency) {
        if (concurrency != null && concurrency >= 0)
            this.concurrency = concurrency;
    }

    public Integer getConcurrency() {
        return this.concurrency;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(Boolean durable) {
        if (durable != null) {
            this.durable = durable;
        }
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getSimpleName() {
        if (this.simpleName != null) {
            return this.simpleName;
        } else {
            return getRubyClassName();
        }
    }

    public void setXAEnabled(boolean xaEnabled) {
        this.xaEnabled = xaEnabled;
    }

    public boolean isXAEnabled() {
        return this.xaEnabled;
    }

    private String rubyClassName;
    private String location;
    private String queueSuffix;
    private int concurrency = 1;
    private boolean durable = true;
    private String simpleName;
    private boolean xaEnabled = true;
}
