require 'torquebox/container/foundation'

module TorqueBox
  module Messaging

    # A service that may be configured with mappings of message
    # consumers to destinations
    class Dispatcher

      def initialize &block
        @container = TorqueBox::Container::Foundation.new
        @container.enable( TorqueBox::Messaging::MessageProcessorHost )
        instance_eval &block if block_given?
      end

      # Option keys are :filter and :config, either as symbol or string
      def map consumer, destination, options = {}
        options = options.inject({}) {|h,(k,v)| h[k.to_s]=v; h} # jruby won't convert symbols to strings
        processor = org.torquebox.messaging.deployers::MessagingYamlParsingDeployer::Parser.subscribe(consumer.to_s, destination.to_s, options)
        processor.ruby_require_path = nil if consumer.is_a? Class
        processors << processor
      end

      def start
        @container.start
        @deployment = @container.deploy( object_id.to_s )
        unit = @container.deployment_unit( @deployment.name )
        processors.each do |processor|
          org.torquebox.mc::AttachmentUtils.multipleAttach( unit, processor, processor.name )
        end
        @container.process_deployments(true)
      end

      def stop
        @container.undeploy( @deployment )
        @container.stop
      end

      def processors
        @processors ||= []
      end

    end
  end
end
