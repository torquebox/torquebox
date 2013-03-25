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

package org.torquebox.core.injection.analysis;


import org.jruby.RubyClass;

public class RubyInjectionUtils {

    /**
     * Convenience method to try real hard to convert an Object into a String.
     * 
     * @param injection
     *            The injection.
     * @return The string, if possible, otherwise <code>null</code>.
     */
    protected static String getString(Object injection) {
        if (injection == null) {
            return null;
        }
        // regular .toString doesn't handle RubyClass instances
        if (injection instanceof RubyClass) {
            return ((RubyClass) injection).getName();
        }
        return injection.toString();
    }

    protected static String getJavaClassName(Object injection) {
        if (injection instanceof RubyClass) {
            return ((RubyClass) injection).callMethod( "java_class").toString();
        } else {
            return injection.getClass().getName();
        }
    }

}
