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
package org.torquebox.soap.core.cxf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.service.model.MessagePartInfo;
import org.jboss.logging.Logger;
import org.torquebox.soap.core.databinding.RubyTypeSpace;
import org.torquebox.soap.core.databinding.RubyXMLStreamDataWriter;

public class RubyDataWriter<T> implements DataWriter<T> {

	private static final Logger log = Logger.getLogger(RubyDataWriter.class);

	@SuppressWarnings("unused")
	private Collection<Attachment> attachments;
	@SuppressWarnings("unused")
	private Schema schema;
	
	private Map<String, Object> properties = new HashMap<String, Object>();

	private RubyXMLStreamDataWriter streamWriter;

	public RubyDataWriter(RubyTypeSpace typeSpace) {
		this.streamWriter = new RubyXMLStreamDataWriter(typeSpace);
	}

	public void setAttachments(Collection<Attachment> attachments) {
		this.attachments = attachments;
	}

	public void setProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public void write(Object object, T output) {
		if (log.isTraceEnabled()) {
			log.trace("write(" + object + ", " + output + ")");
		}
		write(object, null, output);
	}

	public void write(Object object, MessagePartInfo partInfo, T output) {
		if (log.isTraceEnabled()) {
			log.trace("write(" + object + ", " + partInfo + ", " + output + ")");
		}

		if (output instanceof XMLStreamWriter) {
			write(object, partInfo, (XMLStreamWriter) output);
		}
	}

	private void write(Object object, MessagePartInfo partInfo, XMLStreamWriter output) {
		try {
			streamWriter.write(output, object, partInfo.getConcreteName());
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new Fault(e);
		}
	}

}
