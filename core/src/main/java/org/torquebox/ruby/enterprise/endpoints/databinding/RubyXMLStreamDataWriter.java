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
package org.torquebox.ruby.enterprise.endpoints.databinding;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.logging.Logger;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.enterprise.endpoints.databinding.complex.RubyAttribute;
import org.torquebox.ruby.enterprise.endpoints.databinding.complex.RubyComplexType;
import org.torquebox.ruby.enterprise.endpoints.databinding.simple.RubyBooleanType;
import org.torquebox.ruby.enterprise.endpoints.databinding.simple.RubyFloatType;
import org.torquebox.ruby.enterprise.endpoints.databinding.simple.RubyIntegerType;
import org.torquebox.ruby.enterprise.endpoints.databinding.simple.RubySimpleType;
import org.torquebox.ruby.enterprise.endpoints.databinding.simple.RubyStringType;

public class RubyXMLStreamDataWriter {

	private Logger log = Logger.getLogger(RubyXMLStreamDataWriter.class);

	private RubyTypeSpace typeSpace;

	private int namespaceCounter = 0;

	public RubyXMLStreamDataWriter(RubyTypeSpace typeSpace) {
		this.typeSpace = typeSpace;
	}
	
	public RubyType determineType(Object object) {
		RubyType type = null;
		
		if ( object instanceof IRubyObject ) {
			String className = ((IRubyObject)object).getMetaClass().getName();
			type = typeSpace.getTypeByClassName( className );
		} else if ( object instanceof String ) {
			return RubyStringType.INSTANCE;
		} else if ( object instanceof Float || object instanceof Double ) {
			return RubyFloatType.INSTANCE;
		} else if ( object instanceof Long || object instanceof Integer || object instanceof Short ) {
			return RubyIntegerType.INSTANCE;
		} else if ( object instanceof Boolean ) {
			return RubyBooleanType.INSTANCE;
		}
		
		return type;
	}

	public void write(XMLStreamWriter output, Object object, QName concreteName) throws XMLStreamException {
		RubyType type = determineType( object );
		
		if (type instanceof RubyComplexType) {
			writeComplexWithType(output, (IRubyObject) object, concreteName, (RubyComplexType) type );
		} else if ( type instanceof RubySimpleType ) {
			writeSimpleWithType( output, object, concreteName, (RubySimpleType<?>) type );
		} else {
			log.warn( "unhandled: " + object + " --> " + concreteName );
		}
	}
	
	public void writeWithType(XMLStreamWriter output, Object object, QName concreteName, RubyType type) throws XMLStreamException {
		if ( type instanceof RubyComplexType ) {
			writeComplexWithType( output, (IRubyObject) object, concreteName, (RubyComplexType) type );
		} else if ( type instanceof RubySimpleType ) {
			writeSimpleWithType( output, object, concreteName, (RubySimpleType<?>) type );
		} else {
			log.warn( "unknown type: " + type );
		}
		
	}
	
	public void writeSimpleWithType(XMLStreamWriter output, Object object, QName concreteName, RubySimpleType<?> type) throws XMLStreamException {
		String textual = type.write( object );
		
		boolean addedNamespace = false;
		if (output.getNamespaceContext().getPrefix(concreteName.getNamespaceURI()) == null) {
			output.writeNamespace("rubyns" + (++namespaceCounter), concreteName.getNamespaceURI());
			addedNamespace = true;
		}
		output.writeStartElement(concreteName.getNamespaceURI(), concreteName.getLocalPart());
		
		output.writeCharacters( textual );
		
		output.writeEndElement();
		if (addedNamespace) {
			--namespaceCounter;
		}
	}
	
	public void writeComplexWithType(XMLStreamWriter output, IRubyObject object, QName concreteName, RubyComplexType type) throws XMLStreamException {
		boolean addedNamespace = false;
		if (output.getNamespaceContext().getPrefix(concreteName.getNamespaceURI()) == null) {
			output.writeNamespace("rubyns" + (++namespaceCounter), concreteName.getNamespaceURI());
			addedNamespace = true;
		}
		output.writeStartElement(concreteName.getNamespaceURI(), concreteName.getLocalPart());
		if (type instanceof RubyComplexType) {
			if ( type.isArraySubclass() ) {
				RubyType memberType = type.getArrayType();
				int len = readRubyArrayLength( object );
				for ( int i = 0 ; i < len ; ++i ) {
					Object member = readRubyArrayMember( object, i);
					writeWithType(output, member, ((RubyComplexType)type).getArrayAttribute().getQName(), memberType );
				}
			} else {
				for (RubyAttribute a : ((RubyComplexType) type).getAttributes()) {
					Object attrValue = readRubyAttributeValue(object, a.getName());
					writeWithType(output, attrValue, a.getQName(), a.getType() );
				}
			}
		}
		output.writeEndElement();
		if (addedNamespace) {
			--namespaceCounter;
		}
	}

	private int readRubyArrayLength(IRubyObject object) {
		Integer length = (Integer) JavaEmbedUtils.invokeMethod(object.getRuntime(), object, "length", new Object[] {}, Integer.class);
		if ( length != null ) {
			return length.intValue();
		}
		
		return 0;
	}
	
	private Object readRubyArrayMember(IRubyObject object, int index) {
		Object member =  JavaEmbedUtils.invokeMethod(object.getRuntime(), object, "[]", new Object[] { new Integer( index )}, Object.class);
		return member;
	}

	private Object readRubyAttributeValue(IRubyObject object, String name) {
		return JavaEmbedUtils.invokeMethod(object.getRuntime(), object, name, new Object[] {}, Object.class);
	}

}
