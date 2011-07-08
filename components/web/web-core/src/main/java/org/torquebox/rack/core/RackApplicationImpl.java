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

package org.torquebox.rack.core;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackEnvironment;
import org.torquebox.rack.spi.RackResponse;

/**
 * Concrete implementation of {@link RackApplication}.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RackApplicationImpl implements RackApplication {

    /** Log. */
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( RackApplicationImpl.class );

    /** Ruby object of the app. */
    private IRubyObject rubyApp;

    /**
     * Construct.
     * 
     * @param ruby
     *            The Ruby interpreter to use for this application.
     * @param rackUpScript
     *            The rackup script.
     */
    public RackApplicationImpl(Ruby ruby, String rackUpScript, VirtualFile rackUpScriptLocation, VirtualFile rackRoot) throws Exception {
        this.rubyApp = rackUp( ruby, rackUpScript, rackUpScriptLocation, rackRoot );
    }

    /**
     * Perform rackup.
     * 
     * @param script
     *            The rackup script.
     */
    private IRubyObject rackUp(Ruby ruby, String script, VirtualFile rackUpScriptLocation, VirtualFile rackRoot) throws Exception {
        StringBuilder fullScript = new StringBuilder();
        if (usesBundler( rackRoot )) {
            fullScript.append( "require %q(bundler/setup)\n" );
        }
        fullScript.append( "require %q(rack)\n" );
        fullScript.append( "Rack::Builder.new{(\n" );
        fullScript.append( script );
        fullScript.append( "\n)}.to_app" );
        IRubyObject app = ruby.executeScript( fullScript.toString(), rackUpScriptLocation.toURL().toString() );
        return app;
    }

    protected boolean usesBundler(VirtualFile rackRoot) {
        return rackRoot.getChild( "Gemfile" ).exists();
    }

    protected IRubyObject getRubyApplication() {
        return this.rubyApp;
    }

    public Ruby getRuby() {
        return this.rubyApp.getRuntime();
    }

    public RackResponse call(RackEnvironment env) {
        IRubyObject response = (RubyArray) JavaEmbedUtils.invokeMethod( this.rubyApp.getRuntime(), this.rubyApp, "call", new Object[] { env.getEnv() }, RubyArray.class );
        return new RackResponseImpl( response );
    }

}
