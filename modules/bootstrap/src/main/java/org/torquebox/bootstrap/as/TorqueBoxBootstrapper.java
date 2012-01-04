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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.torquebox.bootstrap.JRubyHomeLocator;

public class TorqueBoxBootstrapper {
    private static final Logger log = Logger.getLogger( "org.torquebox.bootstrap" );

    static {
        log.info( "Bootstrapping TorqueBox" );

        String jrubyHome = JRubyHomeLocator.determineJRubyHome( true );

        if (jrubyHome == null) {
            log.fatal( "Unable to find a JRuby Home" );
        } else {

            log.info( "===> " + jrubyHome );

            System.setProperty( "jruby.home", jrubyHome );

            File libDir = new File( jrubyHome, "lib" );

            List<ResourceLoaderSpec> loaderSpecs = new ArrayList<ResourceLoaderSpec>();

            for (File child : libDir.listFiles()) {
                if (child.getName().endsWith( ".jar" )) {
                    log.info( "Adding: " + child );
                    try {
                        ResourceLoader loader = ResourceLoaders.createJarResourceLoader( child.getName(), new JarFile( child ) );
                        ResourceLoaderSpec loaderSpec = ResourceLoaderSpec.createResourceLoaderSpec( loader );
                        loaderSpecs.add( loaderSpec );
                    } catch (IOException e) {
                        log.error( e );
                    }
                }
            }

            swizzleResourceLoaders( loaderSpecs );
        }
    }
    
    private static ResourceLoader[] getExistingResourceLoaders() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Module module = Module.forClass( TorqueBoxBootstrapper.class );
        ModuleClassLoader cl = module.getClassLoader();

        Method method = ModuleClassLoader.class.getDeclaredMethod( "getResourceLoaders" );
        method.setAccessible( true );
        Object result = method.invoke( cl );

        return (ResourceLoader[]) result;

    }

    private static void swizzleResourceLoaders(List<ResourceLoaderSpec> loaderSpecs) {

        Module module = Module.forClass( TorqueBoxBootstrapper.class );
        ModuleLoader moduleLoader = module.getModuleLoader();

        try {
            for (ResourceLoader each : getExistingResourceLoaders()) {
                loaderSpecs.add( ResourceLoaderSpec.createResourceLoaderSpec( each ) );
            }

            Method method = ModuleLoader.class.getDeclaredMethod( "setAndRefreshResourceLoaders", Module.class, Collection.class );
            method.setAccessible( true );
            log.info( "Swizzle: " + loaderSpecs );
            method.invoke( moduleLoader, module, loaderSpecs );

            Method refreshMethod = ModuleLoader.class.getDeclaredMethod( "refreshResourceLoaders", Module.class );
            refreshMethod.setAccessible( true );
            refreshMethod.invoke( moduleLoader, module );

            Method relinkMethod = ModuleLoader.class.getDeclaredMethod( "relink", Module.class );
            relinkMethod.setAccessible( true );
            relinkMethod.invoke( moduleLoader, module );
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
