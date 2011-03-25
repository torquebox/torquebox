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

package org.torquebox.interp.deployers;

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig.CompileMode;

import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.core.RubyRuntimeFactoryImpl;

import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: CLASSLOADER
 *    In: RubyRuntimeMetaData
 *   Out: DeployerRuby
 * </pre>
 * 
 * Deployer which actually creates a RubyRuntimeFactory and attaches it to the
 * unit.
 * 
 * <p>
 * This deployer actually creates an instance of RubyRuntimeFactory and attaches
 * it to the unit.
 * </p>
 * 
 * @author Bob McWhirter
 */
public class RubyRuntimeFactoryDeployer extends AbstractSimpleVFSRealDeployer<RubyRuntimeMetaData> {

    /** Kernel. */
    private Kernel kernel;

    /** Should use JRUBY_HOME environment variable? */
    private boolean useJRubyHomeEnvVar = true;

    /** Construct. */
    public RubyRuntimeFactoryDeployer() {
        super( RubyRuntimeMetaData.class );
        addInput(RubyApplicationMetaData.class);
        addInput( ClassLoader.class );
        setStage( DeploymentStages.CLASSLOADER );
        addOutput( Ruby.class );
    }

    /**
     * Set the kernel.
     * 
     * @param kernel
     *            The kernel.
     */
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Get the kernel.
     * 
     * @return The kernel.
     */
    public Kernel getKernel() {
        return this.kernel;
    }

    public void setUseJRubyHomeEnvVar(boolean useJRubyHomeEnvVar) {
        this.useJRubyHomeEnvVar = useJRubyHomeEnvVar;
    }

    public boolean useJRubyHomeEnvVar() {
        return this.useJRubyHomeEnvVar;
    }

    @Override
    public void deploy(VFSDeploymentUnit unit, RubyRuntimeMetaData metaData) throws DeploymentException {

        String beanName = AttachmentUtils.beanName( unit, RubyRuntimeFactory.class );
        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName, RubyRuntimeFactoryImpl.class.getName() );

        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( metaData.getRuntimeInitializer() );

        List<String> loadPaths = new ArrayList<String>();

        for (RubyLoadPathMetaData loadPath : metaData.getLoadPaths()) {
            loadPaths.add( loadPath.getURL().toExternalForm() );
        }
        
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        factory.setLoadPaths( loadPaths );
        factory.setKernel( this.kernel );
        factory.setApplicationName( rubyAppMetaData.getApplicationName() );
        factory.setClassLoader( unit.getClassLoader() );
        factory.setUseJRubyHomeEnvVar( this.useJRubyHomeEnvVar );
        factory.setApplicationEnvironment( metaData.getEnvironment() );

        if (metaData.getVersion() == RubyRuntimeMetaData.Version.V1_9) {
            factory.setRubyVersion( CompatVersion.RUBY1_9 );
        } else {
            factory.setRubyVersion( CompatVersion.RUBY1_8 );
        }

        RubyRuntimeMetaData.CompileMode compileMode = metaData.getCompileMode();

        if (compileMode == RubyRuntimeMetaData.CompileMode.JIT) {
            factory.setCompileMode( CompileMode.JIT );
        } else if (compileMode == RubyRuntimeMetaData.CompileMode.OFF) {
            factory.setCompileMode( CompileMode.OFF );
        } else if (compileMode == RubyRuntimeMetaData.CompileMode.FORCE) {
            factory.setCompileMode( CompileMode.FORCE );
        }

        KernelController controller = this.kernel.getController();

        try {
            controller.install( builder.getBeanMetaData(), factory );
        } catch (Throwable e) {
            throw new DeploymentException( e );
        }

        unit.addAttachment( DeployerRuby.class, new DeployerRuby( factory ) );

    }

    public void undeploy(VFSDeploymentUnit unit, RubyRuntimeMetaData md) {
        String beanName = AttachmentUtils.beanName( unit, RubyRuntimeFactory.class );
        KernelController controller = this.kernel.getController();
        controller.uninstall( beanName );
    }

}
