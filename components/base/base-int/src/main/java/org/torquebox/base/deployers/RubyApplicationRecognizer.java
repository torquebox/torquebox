package org.torquebox.base.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class RubyApplicationRecognizer extends AbstractRecognizer {

    public RubyApplicationRecognizer() {
        setStage(DeploymentStages.PRE_PARSE);
        addOutput(RubyApplicationMetaData.class);
        setRelativeOrder(5000);
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy((VFSDeploymentUnit) unit);
        }
    }

    @Override
    protected void handle(VFSDeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment(RubyApplicationMetaData.class);

        if (rubyAppMetaData == null) {
            log.debug("Initializing ruby application: " + unit);
            rubyAppMetaData = new RubyApplicationMetaData();
            rubyAppMetaData.setRoot(unit.getRoot());

            unit.addAttachment(RubyApplicationMetaData.class, rubyAppMetaData);
        } else {
            log.debug("Ruby application already initialized: " + unit);
        }
    }
    
    static boolean isRubyApplication(VirtualFile file) {
        return  hasAnyOf(file, 
                  "torquebox.yml", 
                  "config/torquebox.yml", 
                  "config.ru", 
                  "config/environment.rb", 
                  "Rakefile", 
                  "Gemfile", 
                  ".bundle/config",
                  "vendor/rails" );
    }

    protected boolean isRecognized(VFSDeploymentUnit unit) {
        return RubyApplicationRecognizer.isRubyApplication(unit.getRoot());
    }

    protected static boolean hasAnyOf(VirtualFile root, String... paths) {
        for (String path : paths) {
            if (root.getChild(path).exists()) {
                return true;
            }
        }
        return false;
    }

}
