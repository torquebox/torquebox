package org.torquebox.messaging.deployers;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.messaging.MessageDrivenConsumerConfig;

public class ConsumersRbParsingDeployer extends
		AbstractVFSParsingDeployer<MessageDrivenConsumerConfig> {

	public ConsumersRbParsingDeployer() {
		super(MessageDrivenConsumerConfig.class);
		setName("consumers.rb");
		setStage(DeploymentStages.POST_CLASSLOADER);
		addInput(Ruby.class);
	}

	@Override
	protected MessageDrivenConsumerConfig parse(VFSDeploymentUnit unit,
			VirtualFile file, MessageDrivenConsumerConfig root)
			throws Exception {

		Ruby ruby = unit.getAttachment(Ruby.class);

		log.info("about to load consumers.rb");
		IRubyObject result = ruby
				.evalScriptlet("require %(torquebox-messaging-container)\n"
						+ "config_src = IO.read %(" + file.toURL() + ")\n"
						+ "eval config_src\n");
		if (result == null) {
			log.info("result is NULL");
		} else {
			log.info("result is " + result + ", " + result.getClass());
			if ( result instanceof RubyArray ) {
				log.info( "walking array" );
				RubyArray array = (RubyArray) result;
				for ( Object each : array ) {
					log.info( "each is " + each + ", " + each.getClass() );
					if ( each instanceof MessageDrivenConsumerConfig ) {
						MessageDrivenConsumerConfig config = (MessageDrivenConsumerConfig) each;
						log.info( "attach " + config );
						unit.addAttachment(MessageDrivenConsumerConfig.class.getName() + "$" + config.getDestinationName() + "$" + config.getRubyClassName(), config, MessageDrivenConsumerConfig.class );
					}
				}
			}
		}

		return null;
	}

}
