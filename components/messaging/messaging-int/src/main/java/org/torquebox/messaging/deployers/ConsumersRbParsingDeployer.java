package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;

public class ConsumersRbParsingDeployer extends AbstractVFSParsingDeployer<MessageProcessorMetaData> {

	public ConsumersRbParsingDeployer() {
		super(MessageProcessorMetaData.class);
		setName("consumers.rb");
		setStage(DeploymentStages.POST_CLASSLOADER);
		addInput(Ruby.class);
	}

	@Override
	protected MessageProcessorMetaData parse(VFSDeploymentUnit unit, VirtualFile file, MessageProcessorMetaData root) throws Exception {

		Ruby ruby = unit.getAttachment(Ruby.class);

		log.info("about to load consumers.rb");
		IRubyObject result = ruby.evalScriptlet("require %(torquebox-messaging-container)\n" + "config_src = IO.read %(" + file.toURL() + ")\n" + "eval config_src\n");
		if (result == null) {
			log.info("result is NULL");
		} else {
			log.info("result is " + result + ", " + result.getClass());
			if (result instanceof RubyArray) {
				log.info("walking array");
				RubyArray array = (RubyArray) result;
				for (Object each : array) {
					log.info("each is " + each + ", " + each.getClass());
					if (each instanceof MessageProcessorMetaData) {
						MessageProcessorMetaData messageProcessorMetaData = (MessageProcessorMetaData) each;
						log.info("attach " + messageProcessorMetaData);
						AttachmentUtils.multipleAttach( unit, messageProcessorMetaData, messageProcessorMetaData.getName() );
					}
				}
			}
		}

		return null;
	}

}
