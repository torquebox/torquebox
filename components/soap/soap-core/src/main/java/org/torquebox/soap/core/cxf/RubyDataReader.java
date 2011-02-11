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

package org.torquebox.soap.core.cxf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.service.model.MessagePartInfo;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.soap.core.databinding.RubyType;
import org.torquebox.soap.core.databinding.RubyTypeSpace;
import org.torquebox.soap.core.databinding.RubyXMLStreamDataReader;

public class RubyDataReader<T> implements DataReader<T> {

	@SuppressWarnings("unused")
	private Collection<Attachment> attachments;
	@SuppressWarnings("unused")
	private Schema schema;

	
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	private RubyTypeSpace typeSpace;
	private RubyRuntimePool runtimePool;
	
	private RubyXMLStreamDataReader reader;

	public RubyDataReader(RubyTypeSpace typeSpace, RubyRuntimePool runtimePool) {
		this.typeSpace = typeSpace;
		this.runtimePool = runtimePool;
		this.reader = new RubyXMLStreamDataReader();
	}

	public Object read(T input) {
		return read(null, input);
	}

	public Object read(MessagePartInfo partInfo, T input) {
		if (input instanceof XMLStreamReader) {
			return read(partInfo, (XMLStreamReader) input);
		}
		return null;
	}

	private Object read(MessagePartInfo partInfo, XMLStreamReader input) {
		Ruby runtime = null;
		RubyType type = typeSpace.getTypeByQName(partInfo.getTypeQName());
		try {
			runtime = runtimePool.borrowRuntime();
			runtime.evalScriptlet( "require %q(" + typeSpace.getRubyPath() + ")" );
			return (IRubyObject) reader.read(runtime, input, type);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if ( runtime != null ) {
				runtimePool.returnRuntime( runtime );
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object read(QName name, T node, Class type) {
		return read(node);
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

}
