package org.torquebox.test.as;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.SimpleAttachable;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class MockDeploymentPhaseContext extends SimpleAttachable implements DeploymentPhaseContext {

    private MockDeploymentUnit deploymentUnit;
    private MockServiceTarget serviceTarget;
    private MockServiceRegistry serviceRegistry;
    private MountHandle mountHandle;
    
    
    public MockDeploymentPhaseContext() {
        this(  new MockServiceRegistry(), "test-unit" );
    }
    
    public void close() {
        if ( this.mountHandle != null ) {
            this.mountHandle.close();
            this.mountHandle = null;
        }
    }
    
    public MockDeploymentPhaseContext(String name, URL content) throws URISyntaxException, IOException {
        this(  new MockServiceRegistry(), name );
        
        VirtualFile root = VFS.getChild( content.toURI() );
        File fileRoot = new File( content.getFile() );
        Closeable handle = VFS.mountReal( fileRoot, root.getChild(  name  ) );
        this.mountHandle = new MountHandle( handle );
        ResourceRoot resourceRoot = new ResourceRoot( root, mountHandle );
        this.deploymentUnit.putAttachment( Attachments.DEPLOYMENT_ROOT, resourceRoot );
    }
    
    public MockDeploymentPhaseContext(MockServiceRegistry serviceRegistry, String name) {
        this.deploymentUnit = new MockDeploymentUnit( serviceRegistry, name );
        this.serviceTarget = new MockServiceTarget();
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public ServiceName getPhaseServiceName() {
        return null;
    }

    @Override
    public ServiceTarget getServiceTarget() {
        return this.serviceTarget;
    }
    
    public MockServiceTarget getMockServiceTarget() {
        return this.serviceTarget;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }
    
    public MockServiceRegistry getMockServiceRegistry() {
        return this.serviceRegistry;
    }

    @Override
    public DeploymentUnit getDeploymentUnit() {
        return this.deploymentUnit;
    }
    
    public MockDeploymentUnit getMockDeploymentUnit() {
        return this.deploymentUnit;
    }

    @Override
    public Phase getPhase() {
        return null;
    }

    @Override
    public <T> void addDependency(ServiceName serviceName, AttachmentKey<T> attachmentKey) {
    }

    @Override
    public <T> void addDependency(ServiceName serviceName, Class<T> type, Injector<T> injector) {
    }

    @Override
    public <T> void addDeploymentDependency(ServiceName serviceName, AttachmentKey<T> attachmentKey) {
    }
}