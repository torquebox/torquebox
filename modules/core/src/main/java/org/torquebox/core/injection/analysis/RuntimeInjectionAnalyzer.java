package org.torquebox.core.injection.analysis;

import java.util.Set;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceController;
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
            for (Injectable each : injectables) {
                ServiceName eachName = each.getServiceName( this.serviceTarget, this.deploymentUnit );
                System.err.println( "SERVICE_NAME: " + eachName );
                ServiceController<?> controller = this.serviceRegistry.getRequiredService( eachName );
                System.err.println( "SERVICE_CONTROLLER: " + controller );
                Object injectedValue = controller.getValue();
                System.err.println( "SERVICE_VALUE: " + injectedValue );
                System.err.println( "KEY: " + each.getKey() );
                registry.getInjector( each.getKey() ).inject( injectedValue );
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
