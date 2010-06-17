
require 'optparse'

require 'torquebox/container/foundation'
require 'torquebox/container/foundation_command'
require 'torquebox/messaging/message_processor_host'
require 'torquebox/messaging/metadata_builder'
require 'torquebox/naming'

module TorqueBox
  module Messaging
    module Commands

      class MessageProcessorHost < TorqueBox::Container::FoundationCommand

        attr_accessor :deploy_files
        def initialize()
          super
          @deploy_files = []
          @deployments = []
          @naming_service = nil
        end

        def configure(container)
          TorqueBox::Naming.configure do |config|
            config.host = @naming_service 
          end
          container.enable( TorqueBox::Messaging::MessageProcessorHost )
        end

        def after_start(container)
          @deploy_files.each do |file|
            deployment = container.deploy( file )
            unit = container.deployment_unit( deployment.name )
            puts "unit is #{unit.inspect}"
            builder = TorqueBox::Messaging::MetaData::Builder.new() 
            builder.evaluate_file( file )
            builder.processors.each do |processor|
              org.torquebox.mc::AttachmentUtils.multipleAttach( unit, processor, processor.name )
            end
            container.process_deployments(true)
            @deployments << deployment
          end
        end

        def before_stop(container)
          @deployments.reverse.each do |deployment|
            container.undeploy( deployment )
          end
        end

        def parser_options(opts)
          opts.on( '-d', '--deploy FILE', 'Deploy a message-processor configuration' ) do |file|
            @deploy_files << file
          end
          opts.on( '-n', '--naming HOST', 'External naming service connection' ) do |naming_service|
            @naming_service = naming_service
          end
          super( opts )
        end

      end

    end
  end
end
