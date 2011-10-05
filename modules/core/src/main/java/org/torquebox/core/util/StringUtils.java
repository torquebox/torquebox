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

package org.torquebox.core.util;


import org.projectodd.polyglot.core.util.StringUtil;

/**
 * Java implementations of ActiveSupport string utilities.
 * 
 * @author Anthony Eden
 * @author Bob McWhirter
 */
public class StringUtils extends StringUtil {

    public static String pathToClassName(String path) {
        return pathToClassName( path, ".rb" );
    }

    public static String pathToClassName(String path, String extension) {
        if (path.startsWith( "/" )) {
            path = path.substring( 1 );
        }

        if (extension != null) {
            if (!extension.startsWith( "." )) {
                extension = "." + extension;
            }

            if (path.endsWith( extension )) {
                path = path.substring( 0, path.length() - extension.length() );
            }
        }
        String className = camelize( path );
        return className;
    }

    public static String classNameToPath(String className) {
        String path = className;
        return path;
    }

}
