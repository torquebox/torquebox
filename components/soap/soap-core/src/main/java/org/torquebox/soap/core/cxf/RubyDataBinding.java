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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.databinding.AbstractDataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.service.Service;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.soap.core.databinding.RubyTypeSpace;
import org.w3c.dom.Node;

public class RubyDataBinding extends AbstractDataBinding {

	private static final Class<?>[] SUPPORTED_READER_FORMATS = new Class[] { Node.class };
	private static final Class<?>[] SUPPORTED_WRITER_FORMATS = new Class[] { Node.class };
	
	private RubyRuntimePool runtimePool;
	private RubyTypeSpace typeSpace;
	
	public RubyDataBinding(RubyRuntimePool runtimePool) {
		this.runtimePool = runtimePool;
	}
	
	public void setRubyTypeSpace(RubyTypeSpace typeSpace) {
		this.typeSpace = typeSpace;
	}
	
	public RubyTypeSpace getRubyTypeSpace() {
		return this.typeSpace;
	}

	public <T> DataReader<T> createReader(Class<T> type) {
		if ( type == XMLStreamReader.class ) {
			return new RubyDataReader<T>( this.typeSpace, this.runtimePool );
		}
		if ( type == Node.class ) {
			return new RubyDataReader<T>( this.typeSpace, this.runtimePool );
		}
		return null;
	}

	public <T> DataWriter<T> createWriter(Class<T> type) {
		if ( type == XMLStreamWriter.class ) {
			return (DataWriter<T>) new RubyDataWriter<T>( this.typeSpace );
		} 
		if ( type == Node.class ) {
			return (DataWriter<T>) new RubyDataWriter<T>( this.typeSpace );
		}
		return null;
	}

	public Class<?>[] getSupportedReaderFormats() {
		return SUPPORTED_READER_FORMATS;
	}

	public Class<?>[] getSupportedWriterFormats() {
		return SUPPORTED_WRITER_FORMATS;
	}

	public void initialize(Service service) {
		// intentionally left blank
	}

}
