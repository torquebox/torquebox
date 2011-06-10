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
