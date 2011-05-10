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

package org.torquebox.core.runtime;

import java.net.URL;


/**
 * Describes a path to use for {@code LOAD_PATH} in a Ruby interpreter.
 * 
 * <p>
 * While the load path is described as a collection of {@link URL} instances,
 * the URLs are intended to be local or VFS-based URLs. It is highly unlikely a
 * {@code http://} URL will work.
 * </p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @see RubyRuntimeMetaData
 * 
 */
public class RubyLoadPathMetaData {

    /** URL of the path. */
    private URL url;

    /** whether classes in path should be auto-loaded */
    private boolean autoload = true;

    /**
     * Construct without a path.
     */
    public RubyLoadPathMetaData() {

    }

    /**
     * Construct with a URL.
     * 
     * @param url
     *            The URL to add to the Ruby load path.
     */
    public RubyLoadPathMetaData(URL url) {
        this.url = url;
    }

    /**
     * Set the URL.
     * 
     * @param url
     *            The URL to add to the Ruby load path.
     */
    public void setURL(URL url) {
        this.url = url;
    }

    /**
     * Retrieve the URL.
     * 
     * n * @return The URL to add to the Ruby load path.
     */
    public URL getURL() {
        return this.url;
    }

    /**
     * Set the autoload preference.
     */
    public void setAutoload(boolean autoload) {
        this.autoload = autoload;
    }

    /**
     * Should classes beneath path be autoloaded?
     */
    public boolean isAutoload() {
        return this.autoload;
    }

    /**
     * Stringification
     */
    public String toString() {
        return this.url == null ? "" : this.url.toExternalForm();
    }
}
