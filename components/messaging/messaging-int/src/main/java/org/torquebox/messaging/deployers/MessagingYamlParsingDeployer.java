package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.deployers.DeployerRuby;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;

public class MessagingYamlParsingDeployer extends AbstractVFSParsingDeployer<MessageProcessorMetaData> {

    public MessagingYamlParsingDeployer() {
        super(MessageProcessorMetaData.class);
        setName("messaging.yml");
        setStage(DeploymentStages.POST_CLASSLOADER);
        addRequiredInput( DeployerRuby.class );
    }

    @Override
    protected MessageProcessorMetaData parse(VFSDeploymentUnit unit, VirtualFile file, MessageProcessorMetaData root) throws Exception {

        Ruby ruby = unit.getAttachment(DeployerRuby.class).getRuby();
        
        try {
            StringBuilder script = new StringBuilder();
            ruby.getLoadService().require( "torquebox/messaging/metadata_builder" );
            script.append("TorqueBox::Messaging::MetaData::Builder.evaluate_file( %q(" + file.toURL() + ") )\n" );
            IRubyObject result = ruby.evalScriptlet(script.toString());
            if (result != null) {
                if (result instanceof RubyArray) {
                    RubyArray array = (RubyArray) result;
                    for (Object each : array) {
                        if (each instanceof MessageProcessorMetaData) {
                            MessageProcessorMetaData messageProcessorMetaData = (MessageProcessorMetaData) each;
                            AttachmentUtils.multipleAttach(unit, messageProcessorMetaData, messageProcessorMetaData.getName());
                        }
                    }
                }
            }
        } catch (RaiseException e) {
            log.error("error reading messaging.yml", e);
            log.info(e.getException());
            throw e;
        }

        return null;
    }

}
