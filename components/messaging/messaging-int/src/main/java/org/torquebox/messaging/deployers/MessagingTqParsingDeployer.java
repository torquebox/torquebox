package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;

public class MessagingTqParsingDeployer extends AbstractVFSParsingDeployer<MessageProcessorMetaData> {

	public MessagingTqParsingDeployer() {
		super(MessageProcessorMetaData.class);
		setName("messaging.tq");
		setStage(DeploymentStages.POST_CLASSLOADER);
		addRequiredInput( Ruby.class );
	}

	@Override
	protected MessageProcessorMetaData parse(VFSDeploymentUnit unit, VirtualFile file, MessageProcessorMetaData root) throws Exception {

		Ruby ruby = unit.getAttachment(Ruby.class);
		
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
			log.error("error reading messaging.rb", e);
			log.info(e.getException());
			throw e;
		}

		return null;
	}

}
