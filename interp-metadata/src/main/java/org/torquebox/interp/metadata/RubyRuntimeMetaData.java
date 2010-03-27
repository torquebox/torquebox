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
package org.torquebox.interp.metadata;

import java.util.LinkedList;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.spi.RuntimeInitializer;

public class RubyRuntimeMetaData {
	
	private VirtualFile baseDir;
	private RuntimeInitializer initializer;
	private List<RubyLoadPathMetaData> loadPaths = new LinkedList<RubyLoadPathMetaData>();

	public RubyRuntimeMetaData() {
		
	}
	
	public void setBaseDir(VirtualFile baseDir) {
		this.baseDir = baseDir;
	}
	
	public VirtualFile getBaseDir() {
		return this.baseDir;
	}
	
	public void setRuntimeInitializer(RuntimeInitializer initializer) {
		this.initializer = initializer;
	}
	
	public RuntimeInitializer getRuntimeInitializer() {
		return this.initializer;
	}
	
	public void prependLoadPath(RubyLoadPathMetaData loadPath) {
		loadPaths.add(0, loadPath);
	}
	
	public void appendLoadPath(RubyLoadPathMetaData loadPath) {
		loadPaths.add( loadPath );
	}
	
	public List<RubyLoadPathMetaData> getLoadPaths() {
		return this.loadPaths;
	}


}
