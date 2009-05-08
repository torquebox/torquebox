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
package org.torquebox.ruby.enterprise.endpoints.cxf;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.torquebox.ruby.enterprise.endpoints.RubyEndpointHandler;

public class RubyEndpointInvoker implements Invoker {

	private RubyEndpointHandler handler;

	public RubyEndpointInvoker(RubyEndpointHandler handler) {
		this.handler = handler;
	}

	public RubyEndpointHandler getHandler() {
		return this.handler;
	}

	public Object invoke(Exchange exchange, Object in) {
		
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        MessagePartInfo partInfo = bop.getOutput().getMessageParts().get( 0 );
        
        QName responseType = partInfo.getTypeQName();
        
		Principal principal = (Principal) exchange.getInMessage().get("wss4j.principal.result");
		
		if (in instanceof MessageContentsList) {
			String operationName = getOperationName(exchange);
			MessageContentsList mcl = (MessageContentsList) in;
			Object request = mcl.get(0);
			Object response = handler.invoke(principal, operationName, request, responseType );
			return new MessageContentsList(response);
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private String getOperationName(Exchange exchange) {
		Map<String, List<String>> headers = (Map<String, List<String>>) exchange.getInMessage().get(Message.PROTOCOL_HEADERS);
		List<String> operationNameHeader = headers.get("SOAPAction");
		String operationName = operationNameHeader.get(0);
		if ( operationName.startsWith( "\"" ) ) {
			operationName = operationName.substring( 1 );
		}
		
		if ( operationName.endsWith( "\"" ) ) {
			operationName = operationName.substring(0, operationName.length() - 1 );
		}
		return operationName;
	}

}
