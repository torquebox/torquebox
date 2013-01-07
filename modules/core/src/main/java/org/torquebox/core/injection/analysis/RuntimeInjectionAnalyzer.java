/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.injection.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jruby.RubyProc;
import org.torquebox.core.component.InjectionRegistry;

public class RuntimeInjectionAnalyzer {

    public RuntimeInjectionAnalyzer(ServiceRegistry serviceRegistry, ServiceTarget serviceTarget, DeploymentUnit deploymentUnit, InjectionAnalyzer analyzer) {
        this.serviceRegistry = serviceRegistry;
        this.serviceTarget = serviceTarget;
        this.deploymentUnit = deploymentUnit;
        this.analyzer = analyzer;
    }

    public Object analyzeAndInject(Object arg) throws Exception {
        
        if (arg instanceof RubyProc) {
            RubyProc proc = (RubyProc) arg;
            InjectionRubyByteCodeVisitor visitor = new InjectionRubyByteCodeVisitor( this.analyzer );
            visitor.assumeMarkerSeen();
            this.analyzer.analyze( proc, visitor.getNodeVisitor() );
            Set<Injectable> injectables = visitor.getInjectables();
            InjectionRegistry registry = new InjectionRegistry();

            List<RuntimeInjectionListener> waitingListeners = new ArrayList<RuntimeInjectionListener>();
            for (Injectable each : injectables) {
                ServiceName eachName = each.getServiceName( this.serviceTarget, this.deploymentUnit );
                ServiceController<?> controller = this.serviceRegistry.getRequiredService( eachName );
                if (controller.getState() == State.UP) {
                    Object injectedValue = controller.getValue();
                    registry.getInjector( each.getKey() ).inject( injectedValue );
                } else {
                    RuntimeInjectionListener listener = new RuntimeInjectionListener( controller, each.getKey() );
                    controller.addListener( listener );
                    controller.setMode( Mode.ACTIVE );
                    waitingListeners.add( listener );
                }
            }
            for (RuntimeInjectionListener each : waitingListeners) {
                each.waitForInjectableness();
                Object value = each.getValue();
                registry.getInjector( each.getKey() ).inject( value );
            }
            registry.merge( proc.getRuntime() );
        }
        return null;
    }

    private ServiceRegistry serviceRegistry;
    private ServiceTarget serviceTarget;
    private DeploymentUnit deploymentUnit;
    private InjectionAnalyzer analyzer;

}
