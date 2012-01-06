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

package org.torquebox.stomp;

import javax.transaction.TransactionManager;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.stilts.stomplet.server.StompletServer;

public class StompletServerService implements Service<StompletServer> {

    public StompletServerService(StompletServer server) {
        this.server = server;
    }

    @Override
    public StompletServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    @Override
    public void start(StartContext context) throws StartException {
        SocketBinding binding = this.bindingInjector.getValue();
        try {
        	this.server.setBindAddress( binding.getSocketAddress().getAddress() );
        	this.server.setPort( binding.getSocketAddress().getPort() );
            this.server.setTransactionManager( this.transactionManagerInjector.getValue() );
            this.server.start();
        } catch (Exception e) {
            context.failed( new StartException( e ) );
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.server.stop();
        } catch (Exception e) {
            // ignore, I guess.
        }
    }
    
    public Injector<TransactionManager> getTransactionManagerInjector() {
        return this.transactionManagerInjector;
    }
    
    public Injector<SocketBinding> getBindingInjector() {
        return this.bindingInjector;
    }


    private StompletServer server;
    private InjectedValue<TransactionManager> transactionManagerInjector = new InjectedValue<TransactionManager>();
    private InjectedValue<SocketBinding> bindingInjector = new InjectedValue<SocketBinding>();
    
}
