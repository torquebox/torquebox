package org.torquebox.core.as;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.as.controller.Extension;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;

public abstract class AbstractBootstrappableExtension implements Extension {
    
    protected void bootstrap() {
        refresh();
        relink();
    }
    
    protected void refresh() {
        Module module = Module.forClass( getClass() );
        ModuleLoader moduleLoader = module.getModuleLoader();

        try {
            Method method = ModuleLoader.class.getDeclaredMethod( "refreshResourceLoaders", Module.class );
            method.setAccessible( true );
            method.invoke( moduleLoader, module );
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

    protected void relink() {
        Module module = Module.forClass( getClass() );
        ModuleLoader moduleLoader = module.getModuleLoader();

        try {
            Method method = ModuleLoader.class.getDeclaredMethod( "relink", Module.class );
            method.setAccessible( true );
            method.invoke( moduleLoader, module );
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
    
    private static Logger log = Logger.getLogger( "org.torquebox.bootstrap" );

}
