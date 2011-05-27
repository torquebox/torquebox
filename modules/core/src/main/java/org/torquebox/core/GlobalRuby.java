package org.torquebox.core;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jruby.Ruby;
import org.torquebox.core.runtime.RubyRuntimeFactory;

public class GlobalRuby implements GlobalRubyMBean, Service<GlobalRuby> {

    @Override
    public GlobalRuby getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        context.execute( new Runnable() {
            public void run() {
                GlobalRuby.this.factory = new RubyRuntimeFactory();
                GlobalRuby.this.factory.setClassLoader( getClass().getClassLoader() );
                GlobalRuby.this.factory.create();
                try {
                    GlobalRuby.this.runtime = GlobalRuby.this.factory.createInstance( "global" );
                    GlobalRuby.this.runtime.useAsGlobalRuntime();
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e )  );
                }
            }
        } );
    }

    @Override
    public void stop(StopContext context) {
        this.runtime.tearDown( false );

    }
    
    public Object evaluate(String script) throws Exception {
        return this.runtime.evalScriptlet( script );
    }
    
    public String evaluateToString(String script) throws Exception {
        Object result = evaluate( script );
        if ( result == null ) {
            return null;
        }
        
        return result.toString();
    }


    private RubyRuntimeFactory factory;
    private Ruby runtime;

}
