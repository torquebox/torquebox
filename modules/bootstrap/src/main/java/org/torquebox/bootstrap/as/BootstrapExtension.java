package org.torquebox.bootstrap.as;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.torquebox.bootstrap.JRubyHomeLocator;

public class BootstrapExtension implements Extension {

    @Override
    public void initialize(ExtensionContext context) {
        log.info( "Bootstrapping TorqueBox" );
        final SubsystemRegistration registration = context.registerSubsystem( SUBSYSTEM_NAME );
        final ModelNodeRegistration subsystem = registration.registerSubsystemModel( BootstrapSubsystemProviders.SUBSYSTEM );
        registration.registerXMLElementWriter( BootstrapSubsystemParser.getInstance() );

        String jrubyHome = JRubyHomeLocator.determineJRubyHome(true);

        if (jrubyHome == null) {
            log.fatal( "Unable to find a JRuby Home" );
            return;
        }
        
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

    private ResourceLoader[] getExistingResourceLoaders() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Module module = Module.forClass( BootstrapExtension.class );
        ModuleClassLoader cl = module.getClassLoader();

        Method method = ModuleClassLoader.class.getDeclaredMethod( "getResourceLoaders" );
        method.setAccessible( true );
        Object result = method.invoke( cl );

        return (ResourceLoader[]) result;

    }

    private void swizzleResourceLoaders(List<ResourceLoaderSpec> loaderSpecs) {

        Module module = Module.forClass( BootstrapExtension.class );
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
    
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping( Namespace.CURRENT.getUriString(), BootstrapSubsystemParser.getInstance() );
    }

    public static final String SUBSYSTEM_NAME = "torquebox-bootstrap";
    private static final Logger log = Logger.getLogger( "org.torquebox.bootstrap" );

}
