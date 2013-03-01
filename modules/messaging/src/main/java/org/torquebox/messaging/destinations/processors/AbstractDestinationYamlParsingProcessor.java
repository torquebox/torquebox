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

package org.torquebox.messaging.destinations.processors;

import org.projectodd.polyglot.messaging.destinations.AbstractDestinationMetaData;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;

import java.util.Map;

public abstract class AbstractDestinationYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public AbstractDestinationYamlParsingProcessor() {
        setSupportsSuffix( true );
        setSupportsRootless( true );
    }

    /**
     * Parses the 'remote' section of a queue or topic
     *
     * @param metaData
     * @param remoteOptions
     */
    protected void parseRemote(AbstractDestinationMetaData metaData, Object remoteOptions) {
        if (remoteOptions == null)
            return;

        Map<String, Object> options = (Map<String, Object>) remoteOptions;

        // Host is required
        metaData.setRemoteHost((String) options.get("host"));

        // Username is optional
        if (options.containsKey("username")) {
            metaData.setUsername((String) options.get("username"));
        }

        // Password is optional
        if (options.containsKey("password")) {
            metaData.setPassword((String) options.get("password"));
        }
    }
}
