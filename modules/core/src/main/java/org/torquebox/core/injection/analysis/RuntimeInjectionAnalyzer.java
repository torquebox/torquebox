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
        System.err.println( "Analyzing: " + arg.getClass() );
        if (arg instanceof RubyProc) {
            RubyProc proc = (RubyProc) arg;
            InjectionRubyByteCodeVisitor visitor = new InjectionRubyByteCodeVisitor( this.analyzer );
            visitor.assumeMarkerSeen();
            this.analyzer.analyze( proc, visitor );
            Set<Injectable> injectables = visitor.getInjectables();
            InjectionRegistry registry = new InjectionRegistry();
            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            ClassLoader appCl = proc.getRuntime().getJRubyClassLoader().getParent();
            try {
                List<RuntimeInjectionListener> waitingListeners = new ArrayList<RuntimeInjectionListener>();
                Thread.currentThread().setContextClassLoader( appCl );
                for (Injectable each : injectables) {
                    ServiceName eachName = each.getServiceName( this.serviceTarget, this.deploymentUnit );
                    ServiceController<?> controller = this.serviceRegistry.getRequiredService( eachName );
                    System.err.println( eachName + " state -> " + controller.getState() );
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
                    System.err.println( each + " +====> " + value );
                    registry.getInjector( each.getKey() ).inject( value );
                }
            } finally {
                Thread.currentThread().setContextClassLoader( originalCl );
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
