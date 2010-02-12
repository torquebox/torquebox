package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jruby.Ruby;
import org.torquebox.messaging.MessageDrivenConsumerConfig;

public class ConsumersRbParsingDeployer extends AbstractVFSParsingDeployer<MessageDrivenConsumerConfig> {

	public ConsumersRbParsingDeployer() {
		super(MessageDrivenConsumerConfig.class);
		setName("consumers.rb");
		setStage( DeploymentStages.POST_CLASSLOADER );
	}

	@Override
	protected MessageDrivenConsumerConfig parse(VFSDeploymentUnit unit, VirtualFile file,
			MessageDrivenConsumerConfig root) throws Exception {
		
		Ruby ruby = unit.getAttachment( Ruby.class );
		
		ruby.evalScriptlet( "require %(torquebox-messaging)\nrequire %(torquebox/messaging/config)'\nload %(" + file.toURL() + ")" );
		
		return null;
	}

}
