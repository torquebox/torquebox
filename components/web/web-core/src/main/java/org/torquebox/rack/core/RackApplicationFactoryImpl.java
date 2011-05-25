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
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.injection.InjectionRegistry;
import org.torquebox.injection.RubyInjectionProxy;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackApplicationFactory;

public class RackApplicationFactoryImpl implements RackApplicationFactory, RubyInjectionProxy {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(  RackApplicationFactoryImpl.class );
    
    private String rackUpScript;
    private VirtualFile rackUpFile;
    private VirtualFile rackRoot;
    private InjectionRegistry injectionRegistry;

    public RackApplicationFactoryImpl() {
    }
    
    public RackApplicationFactoryImpl(String rackUpScript, VirtualFile rackUpScriptLocation, VirtualFile rackRoot) {
        this.rackUpScript = rackUpScript;
        this.rackUpFile = rackUpScriptLocation;
        this.rackRoot = rackRoot;
    }
    
    public void setRackUpScript(String rackUpScript) {
        this.rackUpScript = rackUpScript;
    }

    public String getRackUpScript() {
        return this.rackUpScript;
    }

    public void setRackUpFile(VirtualFile rackUpScriptLocation) {
        this.rackUpFile = rackUpScriptLocation;
    }

    public VirtualFile getRackUpFile() {
        return this.rackUpFile;
    }

    public void setRackRoot(VirtualFile rackRoot) {
        this.rackRoot = rackRoot;
    }

    public VirtualFile getRackRoot() {
        return this.rackRoot;
    }

    public RackApplication createRackApplication(Ruby ruby) throws Exception {

        IRubyObject rubyRackApp = null;
        RackApplication rackApp = null;

        RubyModule torqueboxModule = ruby.getClassFromPath( "TorqueBox" );
        if (torqueboxModule.getConstantNames().contains( "TORQUEBOX_RACK_APP" )) {
            rubyRackApp = torqueboxModule.getConstant( "TORQUEBOX_RACK_APP" );
        }

        if ((rubyRackApp == null) || (rubyRackApp.isNil())) {
            mergeInjections( ruby );
            rackApp = new RackApplicationImpl( ruby, rackUpScript, rackUpFile, rackRoot );
            rubyRackApp = JavaEmbedUtils.javaToRuby( ruby, rackApp );
            torqueboxModule.setConstant( "TORQUEBOX_RACK_APP", rubyRackApp );
        } else {
            rackApp = (RackApplication) JavaEmbedUtils.rubyToJava( rubyRackApp );
        }
        return rackApp;
    }
    
    protected void mergeInjections(Ruby ruby) {
        if (this.injectionRegistry != null) {
            this.injectionRegistry.merge( ruby );
        }
    }

    @Override
    public void setInjectionRegistry(InjectionRegistry injectionRegistry) {
        this.injectionRegistry = injectionRegistry;
    }
    
    public InjectionRegistry getInjectionRegistry() {
        return this.injectionRegistry;
    }

}
