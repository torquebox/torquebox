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

package org.torquebox.core.util;


import java.util.ArrayList;
import java.util.List;

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

    /**
     * Parse a String into List<String> similar to how the JVM parses commandline
     * arguments into String[] args. This is basically the logic from
     * jruby-launcher's argparser.cpp ported to Java.
     */
    public static List<String> parseCommandLineOptions(String options) {
        List<String> optionsList = new ArrayList<String>();
        if (options != null && options.length() > 0) {
            options = options.trim();
            if (options.charAt( 0 ) == '"' || options.charAt( 0 ) == '\'') {
                char quote = options.charAt( 0 );
                if (options.charAt( options.length() - 1 ) == quote) {
                    options = options.substring( 1, options.length() - 1 );
                }
            }

            int start = 0, pos = 0;
            while((pos = options.indexOf( ' ', start )) != -1) {
                String part = options.substring( start, pos );
                if (part.length() > 0) {
                    optionsList.add( part );
                }
                start = pos + 1;
            }
            if (start < options.length()) {
                String part = options.substring( start );
                if (part.length() > 0) {
                    optionsList.add( part );
                }
            }
        }
        return optionsList;
    }

}
