package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.core.RubyRuntimeFactoryImpl;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;

public class MessagingRbParsingDeployer extends AbstractVFSParsingDeployer<MessageProcessorMetaData> {

	public MessagingRbParsingDeployer() {
		super(MessageProcessorMetaData.class);
		setName("messaging.rb");
		setStage(DeploymentStages.POST_CLASSLOADER);
		addInput(Ruby.class);
	}

	@Override
	protected MessageProcessorMetaData parse(VFSDeploymentUnit unit, VirtualFile file, MessageProcessorMetaData root) throws Exception {

		Ruby ruby = unit.getAttachment(Ruby.class);

		if (ruby == null) {
			RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
			ruby = factory.create();
			unit.addAttachment(Ruby.class, ruby);
		}

		try {
			StringBuilder script = new StringBuilder();
			script.append("require %(org/torquebox/messaging/deployers/torquebox-gateway)\n");
			script.append("require %(vfs)\n");
			script.append("config_src = IO.read %(" + file.toURL() + ")\n");
			script.append("eval config_src\n");
			log.info("SCRIPT\n" + script);
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
			log.error("error reading messaging.rb", e);
			log.info(e.getException());
			throw e;
		}

		return null;
	}

}
