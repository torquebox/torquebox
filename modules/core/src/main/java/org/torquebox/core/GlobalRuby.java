package org.torquebox.core;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jruby.Ruby;
import org.torquebox.core.runtime.RubyRuntimeFactory;

/**
 * A singleton (per-AS) service providing a "global" Ruby interpreter.
 * 
 * <p>
 * At the current time, the primary use of the global ruby service is simply to
 * set JRuby's notion of a global interpreter to one of our choosing, instead of
 * the first-created application-specific interpreter.
 * </p>
 * 
 * @author Bob McWhirter
 * 
 */
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
                    context.failed( new StartException( e ) );
                }
            }
        } );
    }

    @Override
    public void stop(StopContext context) {
        this.runtime.tearDown( false );

    }

    /**
     * Evaluate a script.
     * 
     * @param script The script to evaluate
     * @return The result of evaluating the script, in its native form.
     */
    public Object evaluate(String script) throws Exception {
        return this.runtime.evalScriptlet( script );
    }

    /**
     * Evaluate a script, convert the result to a string.
     * 
     * @param script The script to evaluate.
     * @return The result of evaluating the script, converted to a string if
     *         non-<code>nil</code>. If the
     *         result is <code>nil</code>, a Java <code>null</code> is returned.
     * 
     */
    public String evaluateToString(String script) throws Exception {
        Object result = evaluate( script );
        if (result == null) {
            return null;
        }

        return result.toString();
    }

    private RubyRuntimeFactory factory;
    private Ruby runtime;

}
