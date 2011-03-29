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
import org.jruby.Ruby;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackApplicationFactory;
import org.torquebox.rack.spi.RackApplicationPool;

public class RackApplicationPoolImpl implements RackApplicationPool {
    
    private static final Logger log = Logger.getLogger( RackApplicationPoolImpl.class );

    private RubyRuntimePool runtimePool;
    private RackApplicationFactory rackFactory;

    public RackApplicationPoolImpl() {
    }

    public RackApplicationPoolImpl(RubyRuntimePool runtimePool, RackApplicationFactory rackFactory) {
        this.runtimePool = runtimePool;
        this.rackFactory = rackFactory;
    }
    
    public void start() {
        log.info(  "start"  );
        log.info( "factory: " + this.rackFactory );
        log.info( "pool: " + this.runtimePool );
    }
    
    public void stop() {
        log.info( "stop" );
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePool;
    }

    public void setRackApplicationFactory(RackApplicationFactory rackFactory) {
        this.rackFactory = rackFactory;
    }

    public RackApplicationFactory getRackApplicationFactory() {
        return this.rackFactory;
    }

    @Override
    public RackApplication borrowApplication() throws Exception {
        Ruby ruby = this.runtimePool.borrowRuntime();

        return getRackApplication( ruby );
    }

    @Override
    public void releaseApplication(RackApplication rackApp) {
        this.runtimePool.returnRuntime( rackApp.getRuby() );
    }

    protected RackApplication getRackApplication(Ruby ruby) throws Exception {
        RackApplication rackApp = this.rackFactory.createRackApplication( ruby );
        return rackApp;
    }

}
