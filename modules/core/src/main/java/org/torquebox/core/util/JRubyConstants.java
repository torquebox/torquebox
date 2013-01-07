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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jruby.runtime.Constants;

/**
 * Util methods for accessing JRuby's CONSTANTS via reflection. This
 * is necessary since String constants get inlined at compile time, and
 * a user may bring their own, different JRuby version via JRUBY_HOME.
 *
 * @author Toby Crawley
 */
public class JRubyConstants {

    public static String getVersion() {
        return (String)getConstant( "VERSION" );
    }

    public static synchronized Object getConstant(String name) {
        Object value = constants.get( name );
        if (value == null) {
            try {
                Field constant = Constants.class.getField( name );
                value = constant.get( Constants.class );
            } catch (IllegalAccessException e) {
                log.errorf(  e, "Unable to access constant: %s", name );
            } catch (NoSuchFieldException e) {
                log.errorf(  e, "Unable to access constant: %s", name );
            }
            constants.put( name, value );

        }
        return value;
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.util" );

    private static Map<String, Object> constants = new HashMap<String, Object>();

}
