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

package org.torquebox.messaging.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.util.StringUtils;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.yaml.snakeyaml.Yaml;

/**
 * Creates MessageProcessorMetaData instances from queues.yml
 */
public class MessagingYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public MessagingYamlParsingProcessor() {
        setSectionName( "messaging" );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObject) throws DeploymentUnitProcessingException {
        try {
            if (dataObject instanceof String) {
                String s = (String) dataObject;
                if (s.trim().length() == 0) {
                    return;
                } else {
                    parse( unit, new Yaml().load( s ) );
                }
            }

            Map<String, Object> data = (Map<String, Object>) dataObject;

            if (data.containsKey( "default_message_encoding" )) {
                RubyAppMetaData appMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
                appMetaData.getEnvironmentVariables().put( "DEFAULT_MESSAGE_ENCODING", (String) data.get( "default_message_encoding" ) );
                data.remove( "default_message_encoding" );
            }

            for (MessageProcessorMetaData metadata : Parser.parseDestinations( data )) {
                unit.addToAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY, metadata );
            }
        } catch (Exception e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    public static class Parser {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        static List<MessageProcessorMetaData> parseDestinations(Map<String, Object> data) {
            List<MessageProcessorMetaData> result = new ArrayList<MessageProcessorMetaData>();
            for (String destination : data.keySet()) {
                Object value = data.get( destination );
                if (value instanceof Map) {
                    result.addAll( parseHandlers( destination, (Map<String, Map>) value ) );
                } else if (value instanceof List) {
                    result.addAll( parseHandlers( destination, (List) value ) );
                } else {
                    result.add( parseHandler( destination, (String) value ) );
                }
            }
            return result;
        }

        static MessageProcessorMetaData parseHandler(String destination, String handler) {
            return subscribe( handler, destination, Collections.EMPTY_MAP );
        }

        @SuppressWarnings("rawtypes")
        static List<MessageProcessorMetaData> parseHandlers(String destination, Map<String, Map> handlers) {
            List<MessageProcessorMetaData> result = new ArrayList<MessageProcessorMetaData>();
            for (String handler : handlers.keySet()) {
                result.add( subscribe( handler, destination, handlers.get( handler ) ) );
            }
            return result;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        static List<MessageProcessorMetaData> parseHandlers(String destination, List handlers) {
            List<MessageProcessorMetaData> result = new ArrayList<MessageProcessorMetaData>();
            for (Object v : handlers) {
                if (v instanceof String) {
                    result.add( parseHandler( destination, (String) v ) );
                } else {
                    result.addAll( parseHandlers( destination, (Map<String, Map>) v ) );
                }
            }
            return result;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public static MessageProcessorMetaData subscribe(String handler, String destination, Map options) {
            if (options == null)
                options = Collections.EMPTY_MAP;
            MessageProcessorMetaData result = new MessageProcessorMetaData();
            result.setDurable( (Boolean) options.get( "durable" ) );
            result.setSynchronous((Boolean) options.get("synchronous"));
            result.setClientID((String) options.get("client_id"));
            result.setDestinationName(destination);
            result.setMessageSelector((String) options.get("selector"));

            // In case this is a synchronous message processor
            // we do not want to receive own replies
            if (result.isSynchronous()) {
                if (result.getMessageSelector() == null) {
                    // Message selector was not provided
                    result.setMessageSelector( "JMSCorrelationID IS NULL" );
                } else {
                    // Message selector was provided, we need to use it
                    result.setMessageSelector( "(JMSCorrelationID IS NULL) AND (" + result.getMessageSelector() + ")" );
                }
            }

            if (options.containsKey("singleton")) {
                result.setSingleton( (Boolean) options.get( "singleton" ) );
            }
            if (options.containsKey("stopped")) {
                result.setStopped( (Boolean) options.get( "stopped" ) );
            }
            if (options.containsKey( "xa" )) {
                result.setXAEnabled( (Boolean ) options.get( "xa" ) );
            }
            result.setRubyClassName( StringUtils.camelize( handler ) );
            result.setRubyRequirePath( StringUtils.underscore( handler ) );
            result.setRubyConfig( (Map) options.get( "config" ) );
            result.setConcurrency( (Integer) options.get( "concurrency" ) );

            return result;
        }

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.messaging" );
}
