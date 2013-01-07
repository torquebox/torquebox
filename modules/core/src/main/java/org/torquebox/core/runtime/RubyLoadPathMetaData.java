/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

import java.io.File;

/**
 * Describes a path to use for {@code LOAD_PATH} in a Ruby interpreter.
 * 
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @see RubyRuntimeMetaData
 * 
 */
public class RubyLoadPathMetaData {

    /**
     * Construct without a path.
     */
    public RubyLoadPathMetaData() {

    }

    /**
     * Construct with a path.
     * 
     * @param path
     *            The path to add to the Ruby load path.
     */
    public RubyLoadPathMetaData(File path) {
        setPath( path );
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
      this.path = path;
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
        return this.path == null ? "" : this.path.toString();
    }
    

    /** the path. */
    private File path;

    /** whether classes in path should be auto-loaded */
    private boolean autoload = true;
}
