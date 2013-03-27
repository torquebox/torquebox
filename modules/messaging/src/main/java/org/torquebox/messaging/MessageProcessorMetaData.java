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

package org.torquebox.messaging;

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class MessageProcessorMetaData {
    
    public static AttachmentKey<AttachmentList<MessageProcessorMetaData>> ATTACHMENTS_KEY = AttachmentKey.createList( MessageProcessorMetaData.class );
    
    public MessageProcessorMetaData() {
    }

    public String getName() {
        return (this.destinationName + "." + this.rubyClassName);
    }

    public void setRubyClassName(String rubyClassName, String rubyRequirePath) {
        this.rubyClassName = rubyClassName;
        this.rubyRequirePath = rubyRequirePath;
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

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationName() {
        return this.destinationName;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public String getMessageSelector() {
        return this.messageSelector;
    }

    public void setRubyConfig(Map<String, Object> rubyConfig) {
        this.rubyConfig = rubyConfig;
    }

    public Map<String, Object> getRubyConfig() {
        return this.rubyConfig;
    }

    public void setConcurrency(Integer concurrency) {
        if (concurrency != null && concurrency >= 0)
            this.concurrency = concurrency;
    }

    public Integer getConcurrency() {
        return this.concurrency;
    }

    public void setDurable(Boolean durable) {
        if (durable != null)
            this.durable = durable;
    }

    public Boolean isDurable() {
        return this.durable;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(Boolean synchronous) {
        if (synchronous != null)
            this.synchronous = synchronous;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }
    
    public boolean isSingleton() {
        return this.singleton;
    }

    public void setXAEnabled(boolean xaEnabled) {
        this.xaEnabled = xaEnabled;
    }

    public boolean isXAEnabled() {
        return this.xaEnabled;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    private String rubyClassName;
    private String rubyRequirePath;
    private String destinationName;
    private String messageSelector;
    private int concurrency = 1;
    private boolean synchronous = false;
    private boolean durable = false; //only has meaning for Topic processors
    private String clientID;         //only has meaning for Topic processors
    private boolean singleton = false;
    private boolean xaEnabled = false;
    private boolean stopped = false;
    private Map<String, Object> rubyConfig;

}
