package org.torquebox.core.app;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.FileLocatingProcessor;
import org.torquebox.core.as.DeploymentNotifier;

public class RubyApplicationRecognizer extends FileLocatingProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        if (!isRubyApplication( root )) {
            return;
        }
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null) {
            rubyAppMetaData = new RubyApplicationMetaData( unit.getName() );
            rubyAppMetaData.setRoot( root );
            unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        }

        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        unit.putAttachment( DeploymentNotifier.DEPLOYMENT_TIME_ATTACHMENT_KEY, System.currentTimeMillis() );
    }

    static boolean isRubyApplication(VirtualFile file) {
        boolean result = hasAnyOf( file, "torquebox.yml", "config/torquebox.yml", "config.ru", "config/environment.rb", "Rakefile", "Gemfile", ".bundle/config", "vendor/rails" );
        return result;
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.app" );

}
