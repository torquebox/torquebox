
require 'optparse'

require 'torquebox/container/foundation'
require 'torquebox/container/foundation_command'
require 'torquebox/messaging/message_broker'
require 'torquebox/naming'
require 'torquebox/naming/naming_service'

module TorqueBox
  module Messaging
    module Commands

      class MessageBroker < TorqueBox::Container::FoundationCommand

        attr_accessor :deploy_files
        def initialize()
          super
          @deploy_files = []
          @deployments = []
          @naming_service = nil
          @standalone = false
          @bind_address = nil
        end

        def configure(container)
          if ( @standalone )
            container.enable( TorqueBox::Naming::NamingService )
          else
            TorqueBox::Naming.configure do |config|
              config.host = @naming_service
            end
          end
          container.enable( TorqueBox::Messaging::MessageBroker )
        end

        def after_start(container)
          require 'vfs'
          @deploy_files.each do |file|
            puts "deploying #{file}"
            deployment = container.deploy( file )
            unit = container.deployment_unit( deployment.name )
            virtual_file = org.jboss.vfs::VFS.child( File.join( Dir.pwd, file ) )
            unit.addAttachment( 'org.torquebox.messaging.metadata.QueueMetaData.altDD', virtual_file )
            container.process_deployments(true)
            puts "deployed #{file}"
            puts "deployment #{deployment.inspect}"
            #puts "queue is #{TorqueBox::Naming['/queues/foo']}"
            @deployments << deployment
          end
        end

        def before_stop(container)
          @deployments.reverse.each do |deployment|
            container.undeploy( deployment.name )
          end
        end

        def parser_options(opts)
          opts.on( '-d', '--deploy FILE', 'Deploy a destination descriptor' ) do |file|
            @deploy_files << file
          end
          opts.on( '-n', '--naming HOST', 'External naming service connection' ) do |naming_service|
            @naming_service = naming_service
          end
          opts.on( '-s', '--standalone', 'Run standalone (include naming)' ) do |host|
            @standalone = true
          end
          opts.on( '-b', '--bind', 'Bind address' ) do |bind_address|
            @bind_address = bind_address
          end
          super( opts )
        end

      end

    end
  end
end
