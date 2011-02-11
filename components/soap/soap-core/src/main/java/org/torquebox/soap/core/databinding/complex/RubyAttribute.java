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

package org.torquebox.soap.core.databinding.complex;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.torquebox.soap.core.databinding.RubyType;

public class RubyAttribute {
	
	private RubyType type;
	private XmlSchemaElement xsdElement;
	private String rubyName;

	public RubyAttribute(RubyType type, XmlSchemaElement xsdElement) {
		this.type = type;
		this.xsdElement = xsdElement;
		
		if ( this.type == null ) {
			throw new RuntimeException( "no type: " + xsdElement.getQName() );
		}
	}
	
	public String getName() {
		return this.xsdElement.getName();
	}
	
	public QName getQName() {
		return this.xsdElement.getQName();
	}
	
	public String getRubyName() {
		if ( this.rubyName == null ) {
			if ( isPossiblyMultiple() ) {
				this.rubyName = pluralize( getName() );
			} else {
				this.rubyName = getName();
			}
		}
		
		return this.rubyName;
	}
	
	public String getInitializerFragment() {
		if ( isPossiblyMultiple() ) {
			return "@" + getRubyName() + " = [] # array of " + type.getName();
		} else if ( isPossiblyNil() ) {
			return "@" + getRubyName() + " = nil # optional " + type.getName();
		} else {
			return "@" + getRubyName() + " = " + type.getNewInstanceFragment();
		}
	}
	
	public boolean isPossiblyMultiple() {
		return this.xsdElement.getMaxOccurs() > 1;
	}
	
	public boolean isPossiblyNil() {
		if ( isPossiblyMultiple() ) {
			return false;
		}
		return this.xsdElement.getMinOccurs() == 0;
	}
	
	public String toString() {
		return "[RubyAttribute: name=" + getName() + "; type=" + this.type + "; xsdElement=" + xsdElement + "]\n" + getInitializerFragment() + "\n";
	}
	
	public static String pluralize(String in) {
		if ( in.endsWith( "s" ) ) {
			return in + "es";
		}
		
		return in + "s";
	}
	
	public static String capitalize(String in) {
		if ( in.length() > 1 ) { 
			return in.substring(0,1).toUpperCase() + in.substring(1);
		} else if ( in.length() == 1 ) {
			return in.toUpperCase();
		}
		return in;
	}

	public long getMinOccurs() {
		return xsdElement.getMinOccurs();
	}
	
	public long getMaxOccurs() {
		return xsdElement.getMaxOccurs();
	}

	public RubyType getType() {
		return this.type;
	}

}
