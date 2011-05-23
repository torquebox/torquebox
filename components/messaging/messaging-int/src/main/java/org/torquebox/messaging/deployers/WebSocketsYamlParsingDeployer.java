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

package org.torquebox.messaging.deployers;

import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.WebSocketMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: websockets.yml
 *   Out: WebSocketMetaData
 * </pre>
 * 
 * Creates TopicMetaData instances from topics.yml
 */
public class WebSocketsYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

	public WebSocketsYamlParsingDeployer() {
		setSectionName( "websockets" );
		addOutput( WebSocketMetaData.class );
		setRelativeOrder( 5000 );
	}

	@SuppressWarnings("unchecked")
	public void parse(VFSDeploymentUnit unit, Object baseData) throws Exception {
		log.info( "Parsing websockets information." );
		Map<String, Object> data = (Map<String, Object>) baseData;
		WebSocketMetaData ws = new WebSocketMetaData();
		if (data.containsKey( "context" ))
			ws.setContext( (String) data.get( "context" ) );
		if (data.containsKey( "port" ))
			ws.setPort( (Integer) data.get( "port" ) );
		if (!data.containsKey( "handler" ))
			throw new DeploymentException( "Must specify a handler for websockets." );
		if (data.containsKey( "rubyConfig" ))
			ws.setRubyConfig( (Map<String, Object>) data.get( "rubyConfig" ) );
		ws.setHandler( (String) data.get( "handler" ) );
		AttachmentUtils.multipleAttach( unit, ws, ws.getContext() );
		log.info( "Done parsing websockets information." );
	}

}
