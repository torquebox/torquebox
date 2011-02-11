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

package org.torquebox.soap.core.databinding.simple;

public class RubyFloatType extends RubySimpleType<Double> {

	public static final RubyFloatType INSTANCE = new RubyFloatType();

	protected RubyFloatType() {
		super("Float");
	}

	public Double read(String input) {
		return Double.parseDouble(input);
	}

	public String write(Object input) {
		if (input instanceof Double || input instanceof Float) {
			return input.toString();
		}
		return null;
	}

}
