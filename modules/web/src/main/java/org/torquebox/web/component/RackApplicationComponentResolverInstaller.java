package org.torquebox.web.component;

import java.io.IOException;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.DeploymentNotifier;
import org.torquebox.core.component.BaseRubyComponentDeployer;
import org.torquebox.core.component.ComponentEval;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.web.as.WebServices;
import org.torquebox.web.rack.RackApplicationMetaData;

public class RackApplicationComponentResolverInstaller extends BaseRubyComponentDeployer {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null || rackAppMetaData == null) {
            return;
        }

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        ComponentEval instantiator = new ComponentEval();
        try {
            instantiator.setCode( getCode( rackAppMetaData.getRackUpScript( root ), rubyAppMetaData.getRoot() ) );
            instantiator.setLocation( rackAppMetaData.getRackUpScriptFile( root ).toURL().toString() );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }

        ServiceName serviceName = WebServices.rackApplicationComponentResolver( unit );
        ComponentResolver resolver = createComponentResolver( unit );
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( RackApplicationComponent.class );
        // Let Rack / Rails handle reloading for the web stack
        resolver.setAlwaysReload( false );
        
        log.info( "Installing Rack app component resolver: " + serviceName );
        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.ON_DEMAND );
        addInjections( phaseContext, resolver, getInjectionPathPrefixes( phaseContext ), builder );
        builder.install();
        
        // Add to our notifier's watch list
        unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName );
    }
    

    protected List<String> getInjectionPathPrefixes(DeploymentPhaseContext phaseContext) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );
        
        List<String> prefixes = defaultInjectionPathPrefixes();
        prefixes.add(  rackAppMetaData.getRackUpScriptLocation() );
        prefixes.add( "app/controllers/" );
        prefixes.add( "app/helpers/" );
        
        return prefixes;
    }


    protected String getCode(String rackUpScript, VirtualFile root) {
        StringBuilder code = new StringBuilder();
        if (usesBundler( root )) {
            code.append( "require %q(bundler/setup)\n" );
        }
        code.append( "require %q(rack)\n" );
        
        // We have to patch Rack's HeaderHash implementation when running under
        // 1.9 mode until http://jira.codehaus.org/browse/JRUBY-5839 is fixed.
        code.append( "if RUBY_VERSION[0,3] == '1.9' && defined?(Rack) && Rack::VERSION == [1,1]\n" );
        code.append( "  class Rack::Utils::HeaderHash < Hash\n" );
        code.append( "    def each\n" );
        code.append( "      super { |k,v| yield([k, v.respond_to?(:to_ary) ? v.to_ary.join(%Q{\n}) : v]) }\n" );
        code.append( "    end\n" );
        code.append( "  end\n" );
        code.append( "end\n" );
    
        code.append( "Rack::Builder.new{(\n" );
        code.append( rackUpScript );
        code.append( "\n)}.to_app" );
        return code.toString();
    }
    
    protected boolean usesBundler(VirtualFile root) {
        return root.getChild( "Gemfile" ).exists();
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.component" );
}
