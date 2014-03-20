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

package org.torquebox.core.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServer;

import jnr.constants.ConstantSet;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.jmx.MBeanRegistrationService;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.jmx.ObjectNameFactory;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.stdio.StdioContext;
import org.projectodd.polyglot.core.processors.ApplicationExploder;
import org.projectodd.polyglot.core.processors.ArchiveStructureProcessor;
import org.projectodd.polyglot.core.processors.DescriptorRootMountProcessor;
import org.torquebox.TorqueBox;
import org.torquebox.TorqueBoxMBean;
import org.torquebox.TorqueBoxStdioContextSelector;
import org.torquebox.core.GlobalRuby;
import org.torquebox.core.GlobalRubyMBean;
import org.torquebox.core.app.processors.AppJarScanningProcessor;
import org.torquebox.core.app.processors.AppKnobYamlParsingProcessor;
import org.torquebox.core.app.processors.ApplicationYamlParsingProcessor;
import org.torquebox.core.app.processors.EnvironmentYamlParsingProcessor;
import org.torquebox.core.app.processors.RubyApplicationDefaultsProcessor;
import org.torquebox.core.app.processors.RubyApplicationInstaller;
import org.torquebox.core.app.processors.RubyApplicationRecognizer;
import org.torquebox.core.app.processors.RubyYamlParsingProcessor;
import org.torquebox.core.datasource.DataSourceServices;
import org.torquebox.core.datasource.processors.DatabaseProcessor;
import org.torquebox.core.datasource.processors.DatabaseYamlParsingProcessor;
import org.torquebox.core.injection.analysis.InjectableHandlerRegistry;
import org.torquebox.core.injection.analysis.RuntimeInjectionAnalyzer;
import org.torquebox.core.injection.processors.CorePredeterminedInjectableInstaller;
import org.torquebox.core.injection.processors.PredeterminedInjectableProcessor;
import org.torquebox.core.pool.processors.PoolingYamlParsingProcessor;
import org.torquebox.core.processor.LoggingPropertiesWorkaroundProcessor;
import org.torquebox.core.processors.TorqueBoxRbProcessor;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;
import org.torquebox.core.runtime.RuntimeRestartScanner;
import org.torquebox.core.runtime.processors.BaseRubyRuntimeInstaller;
import org.torquebox.core.runtime.processors.RubyNamespaceContextSelectorProcessor;
import org.torquebox.core.runtime.processors.RubyRuntimeFactoryInstaller;
import org.torquebox.core.runtime.processors.RuntimePoolInstaller;
import org.torquebox.core.runtime.processors.RuntimeRestartProcessor;

class CoreSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) {
        model.get( "injector" ).setEmptyObject();
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {

        final InjectableHandlerRegistry registry = new InjectableHandlerRegistry();

        try {
            addCoreServices( context, verificationHandler, newControllers, registry );
            addTorqueBoxStdioContext();
            workaroundJRubyConstantSetRaceCondition();
        } catch (Exception e) {
            throw new OperationFailedException( e, null );
        }

        context.addStep( new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                addDeploymentProcessors( processorTarget, registry );
            }
        }, OperationContext.Stage.RUNTIME );

    }

    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget, final InjectableHandlerRegistry registry) {

        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 0, new DescriptorRootMountProcessor( "-knob.yml" ) );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 0, new ArchiveStructureProcessor( ".knob" ) );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 800, new AppKnobYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 810, new AppJarScanningProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 900, new RubyApplicationRecognizer() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 910, new TorqueBoxYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 920, new TorqueBoxRbProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 930, new ApplicationYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 940, new EnvironmentYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 950, new PoolingYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 960, new RubyYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 970, new RubyApplicationDefaultsProcessor() );
        if (DataSourceServices.enabled) {
            processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, 980, new DatabaseYamlParsingProcessor() );
        }

        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.PARSE, 100, new ApplicationExploder() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.PARSE, 4000, new BaseRubyRuntimeInstaller() );

        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, 0, new CoreDependenciesProcessor() );
        // processorTarget.addDeploymentProcessor( Phase.DEPENDENCIES, 10, new JdkDependenciesProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.CONFIGURE_MODULE, 1000, new PredeterminedInjectableProcessor( registry ) );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.CONFIGURE_MODULE, 1001, new CorePredeterminedInjectableInstaller() );
        processorTarget.addDeploymentProcessor(  CoreExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 1, new LoggingPropertiesWorkaroundProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 110, new RubyNamespaceContextSelectorProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 5000, new DatabaseProcessor() );

        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.INSTALL, 0, new RubyRuntimeFactoryInstaller() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.INSTALL, 10, new RuntimePoolInstaller() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.INSTALL, 20, new RuntimeRestartProcessor() );
        processorTarget.addDeploymentProcessor( CoreExtension.SUBSYSTEM_NAME, Phase.INSTALL, 9000, new RubyApplicationInstaller() );
    }

    protected void addCoreServices(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers,
            InjectableHandlerRegistry registry) throws Exception {
        addTorqueBoxService( context, verificationHandler, newControllers );
        addGlobalRubyServices( context, verificationHandler, newControllers );
        addInjectionServices( context, verificationHandler, newControllers, registry );
        addRuntimeRestartScannerService( context, verificationHandler, newControllers );
    }

    @SuppressWarnings("serial")
    protected void addTorqueBoxService(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws IOException {
        TorqueBox torqueBox = new TorqueBox();

        newControllers.add( context.getServiceTarget().addService( CoreServices.TORQUEBOX, torqueBox )
                .setInitialMode( Mode.ACTIVE )
                .addListener( verificationHandler )
                .install() );

        String mbeanName = ObjectNameFactory.create( "torquebox", new Hashtable<String, String>() {
            {
                put( "type", "version" );
            }
        } ).toString();

        ServiceName mbeanServiceName = CoreServices.TORQUEBOX.append( "mbean" );
        MBeanRegistrationService<TorqueBoxMBean> mbeanService = new MBeanRegistrationService<TorqueBoxMBean>( mbeanName, mbeanServiceName );
        newControllers.add( context.getServiceTarget().addService( mbeanServiceName, mbeanService )
                .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                .addDependency( CoreServices.TORQUEBOX, TorqueBoxMBean.class, mbeanService.getValueInjector() )
                .addListener( verificationHandler )
                .setInitialMode( Mode.PASSIVE )
                .install() );
    }

    @SuppressWarnings("serial")
    protected void addGlobalRubyServices(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {
        newControllers.add( context.getServiceTarget().addService( CoreServices.GLOBAL_RUBY, new GlobalRuby() )
                .addListener( verificationHandler )
                .setInitialMode( Mode.ACTIVE )
                .install() );

        String mbeanName = ObjectNameFactory.create( "torquebox", new Hashtable<String, String>() {
            {
                put( "type", "runtime" );
            }
        } ).toString();

        ServiceName mbeanServiceName = CoreServices.GLOBAL_RUBY.append( "mbean" );
        MBeanRegistrationService<GlobalRubyMBean> mbeanService = new MBeanRegistrationService<GlobalRubyMBean>( mbeanName, mbeanServiceName );
        newControllers.add( context.getServiceTarget().addService( mbeanServiceName, mbeanService )
                .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                .addDependency( CoreServices.GLOBAL_RUBY, GlobalRubyMBean.class, mbeanService.getValueInjector() )
                .addListener( verificationHandler )
                .setInitialMode( Mode.PASSIVE )
                .install() );
    }

    protected void addInjectionServices(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers,
            InjectableHandlerRegistry registry) {
        newControllers.add( context.getServiceTarget().addService( CoreServices.INJECTABLE_HANDLER_REGISTRY, registry )
                .addListener( verificationHandler )
                .setInitialMode( Mode.PASSIVE )
                .install() );

        RuntimeInjectionAnalyzer runtimeInjectionAnalyzer = new RuntimeInjectionAnalyzer();
        Service<RuntimeInjectionAnalyzer> runtimeService = new ValueService<RuntimeInjectionAnalyzer>( new ImmediateValue<RuntimeInjectionAnalyzer> ( runtimeInjectionAnalyzer ) );
        newControllers.add( context.getServiceTarget().addService( CoreServices.RUNTIME_INJECTION_ANALYZER, runtimeService )
                .addListener( verificationHandler )
                .install() );
    }
    
    protected void addRuntimeRestartScannerService(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws IOException {
        RuntimeRestartScanner service = new RuntimeRestartScanner();
        newControllers.add( context.getServiceTarget().addService( CoreServices.RUNTIME_RESTART_SCANNER, service)
                .addListener( verificationHandler )
                .install() );
    }

    protected void addTorqueBoxStdioContext() {
        // Grab the existing AS7 StdioContext
        final StdioContext defaultContext = StdioContext.getStdioContext();
        // Uninstall to reset System.in, .out, .err to default values
        StdioContext.uninstall();
        // Create debug StdioContext based on System streams
        final StdioContext debugContext = StdioContext.create( System.in, System.out, System.err );
        TorqueBoxStdioContextSelector selector = new TorqueBoxStdioContextSelector( defaultContext, debugContext );

        StdioContext.install();
        StdioContext.setStdioContextSelector( selector );
    }

    protected void workaroundJRubyConstantSetRaceCondition() {
        ConstantSet.getConstantSet( "AddressFamily" );
        ConstantSet.getConstantSet( "Errno" );
        ConstantSet.getConstantSet( "Fcntl" );
        ConstantSet.getConstantSet( "INAddr" );
        ConstantSet.getConstantSet( "IPProto" );
        ConstantSet.getConstantSet( "NameInfo" );
        ConstantSet.getConstantSet( "OpenFlags" );
        ConstantSet.getConstantSet( "PRIO" );
        ConstantSet.getConstantSet( "ProtocolFamily" );
        ConstantSet.getConstantSet( "RLIM" );
        ConstantSet.getConstantSet( "RLIMIT" );
        ConstantSet.getConstantSet( "Shutdown" );
        ConstantSet.getConstantSet( "Signal" );
        ConstantSet.getConstantSet( "Sock" );
        ConstantSet.getConstantSet( "SocketLevel" );
        ConstantSet.getConstantSet( "SocketOption" );
        ConstantSet.getConstantSet( "Sysconf" );
        ConstantSet.getConstantSet( "TCP" );
        ConstantSet.getConstantSet( "WaitFlags" );
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    public CoreSubsystemAdd() {
    }

    static final CoreSubsystemAdd ADD_INSTANCE = new CoreSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.core.as" );

}
