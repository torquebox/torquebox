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
