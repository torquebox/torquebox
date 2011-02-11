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

package org.torquebox.soap.core.databinding;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.soap.core.databinding.complex.RubyAttribute;
import org.torquebox.soap.core.databinding.complex.RubyComplexType;
import org.torquebox.soap.core.databinding.simple.RubySimpleType;

public class RubyXMLStreamDataReader {
	public RubyXMLStreamDataReader() {
	}

	public Object read(Ruby runtime, XMLStreamReader input, RubyType type) throws XMLStreamException {
		QName name = input.getName();
		Object result = null;
		if (type instanceof RubyComplexType) {
			result = readComplex(runtime, input, (RubyComplexType) type);
		} else if (type instanceof RubySimpleType) {
			result = readPrimitive(input, (RubySimpleType<?>) type);
		}
		skipToEnd(input, name);
		return result;
	}

	public Object readPrimitive(XMLStreamReader input, RubySimpleType<?> type) throws XMLStreamException {
		QName name = input.getName();
		
		readXMLAttributesAndNamespaces(input);
		
		String text = collectText(input);
		Object result = type.read( text );
		
		skipToEnd( input , name);
		
		return result;
	}
	
	public String collectText(XMLStreamReader input) throws XMLStreamException {
		int event = input.next();
		
		StringBuilder text = new StringBuilder();
		
		while ( event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA || event == XMLStreamConstants.SPACE ) {
			text.append( input.getText() );
			event = input.next();
		}
		
		return text.toString();
	}

	public Object readComplex(Ruby runtime, XMLStreamReader input, RubyComplexType type) throws XMLStreamException {
		//QName name = input.getName();

		readXMLAttributesAndNamespaces(input);

		while (input.next() != XMLStreamConstants.START_ELEMENT) {
			if (input.getEventType() == XMLStreamConstants.END_ELEMENT) {
				return null;
			}
		}

		IRubyObject rubyObject = null;

		if (type.isArraySubclass()) {
			rubyObject = createRubyObject(runtime, type);
			RubyAttribute memberAttr = type.getArrayAttribute();
			while (input.getEventType() == XMLStreamConstants.START_ELEMENT) {
				readArrayMember(input, rubyObject, memberAttr);
				input.nextTag();
			}
			// FIXME
			// result = rubyObject;
		} else {
			rubyObject = createRubyObject(runtime, type);
			while (input.getEventType() == XMLStreamConstants.START_ELEMENT) {
				readAttribute(runtime, input, type, rubyObject);
				input.nextTag();
			}
			// result = rubyObject;
		}

		return rubyObject;
	}

	private void skipToEnd(XMLStreamReader input, QName name) throws XMLStreamException {
		int type = 0;
		while (true) {
			type = input.getEventType();

			switch (type) {
			case (XMLStreamConstants.START_ELEMENT):
				break;
			case (XMLStreamConstants.END_ELEMENT):
				if (input.getName().equals(name)) {
					//log.info("  RETURN");
				} else {
					//log.info("  SKIP");
				}
				return;
			}
			type = input.next();
		}
	}

	private void readArrayMember(XMLStreamReader input, IRubyObject rubyObject, RubyAttribute arrayAttr)
			throws XMLStreamException {
		String name = input.getName().getLocalPart();

		if (!arrayAttr.getName().equals(name)) {
			throw new XMLStreamException("expected <" + arrayAttr.getName() + "> but found <" + name + ">");
		}

		RubyType memberType = arrayAttr.getType();

		Object memberValue = read(rubyObject.getRuntime(), input, memberType);

		addRubyArrayMember(rubyObject, memberValue);
	}

	private void addRubyArrayMember(IRubyObject rubyObject, Object memberValue) {
		JavaEmbedUtils.invokeMethod(rubyObject.getRuntime(), rubyObject, "<<", new Object[] { memberValue }, void.class);
	}

	private void readAttribute(Ruby runtime, XMLStreamReader input, RubyComplexType ownerType, IRubyObject rubyObject)
			throws XMLStreamException {
		String name = input.getName().getLocalPart();
		RubyAttribute rubyAttr = ownerType.getAttribute(name);

		if (rubyAttr == null) {
			throw new XMLStreamException("no attribute for <" + name + ">");
		}

		RubyType attrType = rubyAttr.getType();

		Object attrValue = read(runtime, input, attrType);

		setRubyAttribute(rubyObject, name, attrValue);
	}

	private void setRubyAttribute(IRubyObject rubyObject, String name, Object attrValue) {
		JavaEmbedUtils.invokeMethod(rubyObject.getRuntime(), rubyObject, name + "=", new Object[] { attrValue }, void.class);
	}

	private int readXMLAttributesAndNamespaces(XMLStreamReader input) throws XMLStreamException {
		int eventType = input.getEventType();
		if (eventType == XMLStreamConstants.ATTRIBUTE || eventType == XMLStreamConstants.NAMESPACE) {
			eventType = input.next();
		}
		return eventType;
	}

	private IRubyObject createRubyObject(Ruby runtime, RubyType type) {
		IRubyObject object = runtime.evalScriptlet(type.getNewInstanceFragment());
		return object;
	}

}
