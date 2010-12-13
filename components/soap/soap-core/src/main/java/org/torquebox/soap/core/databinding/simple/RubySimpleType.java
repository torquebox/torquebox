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
package org.torquebox.soap.core.databinding.simple;

import org.torquebox.soap.core.databinding.RubyType;
import org.torquebox.soap.core.databinding.RubyTypeSpace;

public abstract class RubySimpleType<T> extends RubyType {

	public RubySimpleType(String name) {
		super(name);
	}

	public abstract T read(String input);

	public abstract String write(Object input);

	@Override
	public boolean isArraySubclass() {
		return false;
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	protected void initialize(RubyTypeSpace typeSpace) {
		// nothing
	}

	public String getNewInstanceFragment() {
		return "nil";
	}

}
