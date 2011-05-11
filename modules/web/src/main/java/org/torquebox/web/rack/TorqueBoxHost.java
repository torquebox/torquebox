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

package org.torquebox.web.rack;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.jboss.logging.Logger;

public class TorqueBoxHost extends StandardHost {
    
    private static final Logger log = Logger.getLogger( TorqueBoxHost.class );

    @Override
    public void addChild(Container child) {
        log.info( "addChild(" + child + ") BEGIN" );
        super.addChild( child );
        log.info( "addChild(" + child + ") END" );
    }

    @Override
    public synchronized void start() throws LifecycleException {
        log.info( "start() BEGIN" );
        super.start();
        log.info( "start() END" );
    }

    @Override
    public synchronized void init() {
        log.info( "init() BEGIN" );
        super.init();
        log.info( "init() END" );
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName oname) throws Exception {
        log.info( "preRegister(" + oname + ") BEGIN" );
        ObjectName result = super.preRegister( server, oname );
        log.info( "preRegister(" + oname + ") END " + result );
        return result;
    }

    @Override
    public synchronized void stop() throws LifecycleException {
        log.info( "stop() BEGIN" );
        super.stop();
        log.info( "stop() END" );
    }

}
