package org.torquebox.core.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.as.services.RubyRuntimeFactoryService;

public class RubyRuntimeFactoryDeployer implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        String deploymentName = phaseContext.getDeploymentUnit().getName();
        
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData != null && runtimeMetaData != null) {
            RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( runtimeMetaData.getRuntimeInitializer() );

            List<String> loadPaths = new ArrayList<String>();

            for (RubyLoadPathMetaData loadPath : runtimeMetaData.getLoadPaths()) {
                loadPaths.add( loadPath.getURL().toExternalForm() );
            }
            
            Module module = unit.getAttachment( Attachments.MODULE );
            
            log.info( "Initializing ruby with classloader: " + module );
            factory.setClassLoader( module.getClassLoader() );
            
            factory.setServiceRegistry(phaseContext.getServiceRegistry());
            factory.setLoadPaths( loadPaths );
            factory.setApplicationName( rubyAppMetaData.getApplicationName() );
            factory.setUseJRubyHomeEnvVar( this.useJRubyHomeEnvVar );
            factory.setApplicationEnvironment(rubyAppMetaData.getEnvironmentVariables());

            if (runtimeMetaData.getVersion() == RubyRuntimeMetaData.Version.V1_9) {
                factory.setRubyVersion( CompatVersion.RUBY1_9 );
            } else {
                factory.setRubyVersion( CompatVersion.RUBY1_8 );
            }

            RubyRuntimeMetaData.CompileMode compileMode = runtimeMetaData.getCompileMode();

            if (compileMode == RubyRuntimeMetaData.CompileMode.JIT) {
                factory.setCompileMode( CompileMode.JIT );
            } else if (compileMode == RubyRuntimeMetaData.CompileMode.OFF) {
                factory.setCompileMode( CompileMode.OFF );
            } else if (compileMode == RubyRuntimeMetaData.CompileMode.FORCE) {
                factory.setCompileMode( CompileMode.FORCE );
            }

            RubyRuntimeFactoryService service = new RubyRuntimeFactoryService( factory );
            ServiceName name = CoreServices.runtimeFactoryName( unit );
            ServiceBuilder<RubyRuntimeFactory> builder = phaseContext.getServiceTarget().addService( name, service );
            log.info( "installing factory service for unit" );
            builder.install();
            log.info( "installed factory service for unit" );

        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        log.info( "undeploy!: " + context );
    }
    
    private boolean useJRubyHomeEnvVar = true;

    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );

}
