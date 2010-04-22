/* Copyright 2009 Red Hat, Inc. */
package org.torquebox.interp.metadata;

import java.net.URL;

/** Describes a path to use for {@code LOAD_PATH} in a Ruby interpreter.
 * 
 * <p>While the load path is described as a collection of {@link URL} instances,
 * the URLs are intended to be local or VFS-based URLs.  It is highly unlike a
 * {@code http://} URL will work.</p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @see RubyRuntimeMetaData
 *
 */
public class RubyLoadPathMetaData {
	
	/** URL of the path. */
	private URL url;

	/** Construct without a path.
	 */
	public RubyLoadPathMetaData() {
		
	}
	
	/** Construct with a URL.
	 * 
	 * @param url The URL to add to the Ruby load path.
	 */
	public RubyLoadPathMetaData(URL url) {
		this.url = url;
	}
	
	/** Set the URL.
	 * 
	 * @param url The URL to add to the Ruby load path.
	 */
	public void setURL(URL url) {
		this.url = url;
	}
	
	/** Retrieve the URL.
	 * 
	 * @return The URL to add to the Ruby load path.
	 */
	public URL getURL() {
		return this.url;
	}

}
