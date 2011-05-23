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

package org.torquebox.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java implementations of ActiveSupport string utilities.
 * 
 * @author Anthony Eden
 * @author Bob McWhirter
 */
public class StringUtils {

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

    public static String underscore(String word) {
        String firstPattern = "([A-Z]+)([A-Z][a-z])";
        String secondPattern = "([a-z\\d])([A-Z])";
        String replacementPattern = "$1_$2";
        word = word.replaceAll( "\\.", "/" ); // replace package separator with
                                              // slash
        word = word.replaceAll( "::", "/" ); // replace package separator with
                                             // slash
        word = word.replaceAll( "\\$", "__" ); // replace $ with two underscores
                                               // for inner classes
        word = word.replaceAll( firstPattern, replacementPattern ); // replace
                                                                    // capital
                                                                    // letter
                                                                    // with
                                                                    // _ plus
                                                                    // lowercase
                                                                    // letter
        word = word.replaceAll( secondPattern, replacementPattern );
        word = word.replace( '-', '_' );
        word = word.toLowerCase();
        return word;
    }

    public static String camelize(String str) {
        Pattern p = Pattern.compile( "\\/(.?)" );
        Matcher m = p.matcher( str );
        while (m.find()) {
            str = m.replaceFirst( "::" + m.group( 1 ).toUpperCase() );
            m = p.matcher( str );
        }

        p = Pattern.compile( "(_)(.)" );
        m = p.matcher( str );
        while (m.find()) {
            str = m.replaceFirst( m.group( 2 ).toUpperCase() );
            m = p.matcher( str );
        }

        if (str.length() > 0) {
            str = str.substring( 0, 1 ).toUpperCase() + str.substring( 1 );
        }
        return str;
    }
    
    public static boolean isBlank(String str) {
    	return str == null || str.isEmpty();
    }

}
