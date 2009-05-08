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
package org.torquebox.ruby.core.runtime;

import java.net.URL;
import java.net.URLClassLoader;

public class LoadPathClassLoader extends URLClassLoader {
	
	private URL[] urls;
	
	public LoadPathClassLoader(URL[] urls, ClassLoader parent) {
		super( urls, parent );
		this.urls = urls;
	}

	@Override
	public URL findResource(String name) {
		URL result = super.findResource(name);
		return result;
	}
	
	public URL[] getURLs() {
		return this.urls;
	}

}
