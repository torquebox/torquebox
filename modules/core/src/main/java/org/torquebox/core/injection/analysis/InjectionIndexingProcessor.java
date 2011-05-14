package org.torquebox.core.injection.analysis;

import java.io.IOException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.runtime.RubyRuntimeMetaData;

public class InjectionIndexingProcessor implements DeploymentUnitProcessor {
    
    public InjectionIndexingProcessor() {
        
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        
        if ( runtimeMetaData == null ) {
            return;
        }
        
        InjectionIndex index = unit.getAttachment( InjectionIndex.ATTACHMENT_KEY );
        
        if ( index == null ) {
            index = new InjectionIndex();
            unit.putAttachment( InjectionIndex.ATTACHMENT_KEY, index );
        }
        
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        
        InjectionAnalyzer analyzer = getAnalyzer();
        
        long startTime = System.currentTimeMillis();
        
        for ( VirtualFile each : root.getChildren() ) {
            if ( shouldProcess( each  ) ) {
                try {
                    analyzer.analyzeRecursively( index, each, runtimeMetaData.getVersion() );
                } catch (IOException e) {
                    log.error( "Error processing file: " + each );
                }
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        log.info(  "Injection scanning took " + elapsed + "ms" );
        
    }
    
    protected boolean shouldProcess(VirtualFile dir) {
        if ( dir.getName().startsWith( "." ) ) {
            return false;
        }
        
        if ( dir.getName().equals(  "vendor"  ) ) {
            return false;
        }
        
        return true;
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }
    
    public InjectableHandlerRegistry getInjectableHandlerRegistry() {
        return this.registry;
    }
    
    protected synchronized InjectionAnalyzer getAnalyzer() {
        if ( this.injectionAnalyzer == null ) {
            this.injectionAnalyzer = new InjectionAnalyzer( this.registry );
        }
        
        return this.injectionAnalyzer;
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.injection.analysis" );
    
    
    private InjectableHandlerRegistry registry = new InjectableHandlerRegistry();
    private InjectionAnalyzer injectionAnalyzer;

}
