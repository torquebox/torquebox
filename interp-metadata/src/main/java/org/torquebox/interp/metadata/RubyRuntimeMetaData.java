/* Copyright 2009 Red Hat, Inc. */
package org.torquebox.interp.metadata;

import java.util.LinkedList;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.spi.RuntimeInitializer;

/** Root configuration for a Ruby interpreter.
 * 
 * <p>A Ruby interpreter is configured with basic information,
 * such as a {@code baseDir} to describe the working directory
 * for execution, a {@link RuntimeInitializer} for performing an
 * interpreter initialization, and a set of load paths to augment
 * the default Ruby {@code LOAD_PATH}.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @see RubyLoadPathMetaData
 *
 */
public class RubyRuntimeMetaData {
	
	/** Base working directory. */
	private VirtualFile baseDir;
	
	/** Optional interpreter initializer. */
	private RuntimeInitializer initializer;
	
	/** Ordered list of paths to add to the Ruby LOAD_PATH. */
	private List<RubyLoadPathMetaData> loadPaths = new LinkedList<RubyLoadPathMetaData>();

	/** Construct.
	 */
	public RubyRuntimeMetaData() {
		
	}
	
	/** Set the base working directory.
	 * 
	 * @param baseDir The base working directory.
	 */
	public void setBaseDir(VirtualFile baseDir) {
		this.baseDir = baseDir;
	}
	
	/** Retrieve the base working directory.
	 * 
	 * @return The base working directory.
	 */
	public VirtualFile getBaseDir() {
		return this.baseDir;
	}
	
	/** Set the interpreter initializer.
	 * 
	 * @param initializer The initializer.
	 */
	public void setRuntimeInitializer(RuntimeInitializer initializer) {
		this.initializer = initializer;
	}
	
	/** Retrieve the interpreter initializer.
	 * 
	 * @return The interpreter initializer.
	 */
	public RuntimeInitializer getRuntimeInitializer() {
		return this.initializer;
	}
	
	/** Prepend an element to the {@code LOAD_PATH}.
	 * 
	 * @param loadPath The path element to prepend.
	 */
	public void prependLoadPath(RubyLoadPathMetaData loadPath) {
		loadPaths.add(0, loadPath);
	}
	
	/** Append an element to the {@code LOAD_PATH}.
	 * 
	 * @param loadPath The path element to append.
	 */
	public void appendLoadPath(RubyLoadPathMetaData loadPath) {
		loadPaths.add( loadPath );
	}
	
	/** Retrieve the list of {@code LOAD_PATH} elements.
	 * 
	 * @return The list of path elements.
	 */
	public List<RubyLoadPathMetaData> getLoadPaths() {
		return this.loadPaths;
	}


}
