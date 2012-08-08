/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.bootstrap.as;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.modules.ResourceLoaderSpec;
import org.projectodd.polyglot.core.util.ResourceLoaderUtil;
import org.torquebox.bootstrap.JRubyHomeLocator;

public class TorqueBoxBootstrapper {
    private static final Logger log = Logger.getLogger( "org.torquebox.bootstrap" );

    static {
        log.debug( "Bootstrapping TorqueBox" );

        String jrubyHome = JRubyHomeLocator.determineJRubyHome( true );

        if (jrubyHome == null) {
            log.fatal( "Unable to find a JRuby Home" );
        } else {

            log.info( "Bootstrapping TorqueBox with a JRuby home of " + jrubyHome );

            System.setProperty( "jruby.home", jrubyHome );

            File libDir = new File( jrubyHome, "lib" );

            if (!libDir.exists()) {
                // we can't throw here, since any exception will be swallowed, and a NoClassDefFoundError thrown
                // instead since we are in a static initializer
                log.fatal( "============================================================================================" );                        
                log.fatal( "********************************************************************************************" );                        
                log.fatal( "No lib dir found in " + jrubyHome + " - confirm that you are setting JRUBY_HOME properly" );
                log.fatal( "********************************************************************************************" );                        
                log.fatal( "============================================================================================" );
            }
            
            List<ResourceLoaderSpec> loaderSpecs = new ArrayList<ResourceLoaderSpec>();

            for (File child : libDir.listFiles()) {
                if (child.getName().endsWith( ".jar" )) {
                    log.debug( "Adding: " + child );
                    try {
                        loaderSpecs.add( ResourceLoaderUtil.createLoaderSpec( child ) );
                    } catch (IOException e) {
                        log.error( e );
                    }
                }
            }

            swizzleResourceLoaders( loaderSpecs );
        }
    }
    
    private static void swizzleResourceLoaders(List<ResourceLoaderSpec> loaderSpecs) {
        try {
            ResourceLoaderUtil.refreshAndRelinkResourceLoaders( TorqueBoxBootstrapper.class, loaderSpecs, true );
        } catch (SecurityException e) {
            log.fatal( e.getMessage(), e );
        } catch (NoSuchMethodException e) {
            log.fatal( e.getMessage(), e );
        } catch (IllegalArgumentException e) {
            log.fatal( e.getMessage(), e );
        } catch (IllegalAccessException e) {
            log.fatal( e.getMessage(), e );
        } catch (InvocationTargetException e) {
            log.fatal( e.getMessage(), e );
        }

    }

}
