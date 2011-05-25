package org.torquebox.jobs;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.SimpleAttachable;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;

public class MockDeploymentUnit extends SimpleAttachable implements DeploymentUnit {
    
    private String name;
    private ServiceName serviceName;
    private MockServiceRegistry serviceRegistry;

    public MockDeploymentUnit(MockServiceRegistry serviceRegistry) {
        this( serviceRegistry, "test-unit");
    }
    
    public MockDeploymentUnit(MockServiceRegistry serviceRegistry, String name) {
        this.name = name;
        this.serviceName = ServiceName.of(  "TEST" ).append( name );
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public ServiceName getServiceName() {
        return this.serviceName;
    }

    @Override
    public DeploymentUnit getParent() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return null;
    }

}
