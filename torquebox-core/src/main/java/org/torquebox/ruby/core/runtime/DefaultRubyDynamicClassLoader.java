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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.virtual.MemoryFileFactory;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.plugins.context.memory.MemoryContextFactory;
import org.torquebox.ruby.core.runtime.spi.RubyDynamicClassLoader;

public class DefaultRubyDynamicClassLoader extends URLClassLoader implements RubyDynamicClassLoader {

	private static final URL[] EMPTY_URL_ARRAY = new URL[]{};

	private URL baseUrl;
	private VirtualFile baseDir;

	private URLClassLoader extension;
	
	private DefaultRubyDynamicClassLoader(URL baseUrl, URL[] urls, ClassLoader parent, VirtualFile baseDir) {
		super(urls, parent);
		MemoryContextFactory.getInstance().createRoot(baseUrl);
		this.baseUrl = baseUrl;
		this.baseDir = baseDir;
	}
	
	private DefaultRubyDynamicClassLoader(DefaultRubyDynamicClassLoader parent) {
		super( EMPTY_URL_ARRAY, parent );
		this.baseUrl = parent.baseUrl;
		this.baseDir = parent.baseDir;
	}
	
	public DefaultRubyDynamicClassLoader createChild() {
		return new DefaultRubyDynamicClassLoader( this );
	}

	public void destroy() {
		MemoryContextFactory.getInstance().deleteRoot(baseUrl);
	}

	public void putFile(String path, String contents) throws MalformedURLException {
		URL fileUrl = new URL(this.baseUrl, path);
		MemoryFileFactory.putFile(fileUrl, contents.getBytes());
	}

	/* (non-Javadoc)
	 * @see org.torquebox.ruby.core.runtime.RubyDynamicClassLoader#addLoadPaths(java.util.List)
	 */
	public void addLoadPaths(List<String> paths) throws URISyntaxException, IOException {
		List<URL> urls = new ArrayList<URL>();

		String prefix = baseDir.toURL().getPath();

		for (String path : paths) {
			URL url = null;
			if (path.startsWith(prefix)) {
				path = path.substring( prefix.length() );
				VirtualFile file = this.baseDir.getChild( path );
				if ( file != null ) {
					url = file.toURL();
				}
			}
			if ( url != null ) {
				urls.add( url );
			}
		}
		
		URL[] urlArray = urls.toArray( new URL[ urls.size() ] );
		
		this.extension = new LoadPathClassLoader( urlArray, extension );
	}

	@Override
	public URL findResource(String name) {

		if (name.startsWith("./")) {
			name = name.substring(2);
		}

		URL result = super.findResource(name);

		if (result == null) {
			try {
				String prefix = baseDir.toURL().getPath();
				if (name.startsWith(prefix)) {
					name = name.substring(prefix.length());
					result = super.findResource(name);
				}
			} catch (MalformedURLException e) {
				return null;
			} catch (URISyntaxException e) {
				return null;
			}
		}
		
		if ( result == null && this.extension != null ) {
			result = extension.findResource(name);
		}
		return result;
	}

	public static DefaultRubyDynamicClassLoader create(String name, Collection<URL> urls, ClassLoader parent, VirtualFile baseDir)
			throws MalformedURLException {
		URL baseUrl = new URL("vfsmemory://" + name + ".ruby.jboss/");

		URL[] urlArray = new URL[urls.size() + 1];

		int i = 0;

		for (URL url : urls) {
			urlArray[i] = url;
			++i;
		}

		urlArray[i] = baseUrl;

		return new DefaultRubyDynamicClassLoader(baseUrl, urlArray, parent, baseDir);

	}

}
