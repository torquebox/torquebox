require 'torquebox-messaging-client'

module TorqueBox
  module Messaging
    module Tasks


      def self.enqueue(task_spec, payload, connect_opts={})
        TaskClient.connect( connect_opts ) do |client|
          client.enqueue( task_spec, payload )
        end 
      end

      class TaskClient

        def self.connect(connect_opts={},&block)
          TaskClient.new( connect_opts, &block )
        end

        def initialize(connect_opts={},&block)
          @client = TorqueBox::Messaging::Client.connect( connect_opts ) 
          if ( block ) 
            begin
              instance_eval &block
            ensure
              @client.close
            end
          end
        end

        def close
          @client.close
        end

        def enqueue(task_spec, payload={})
          if ( task_spec =~ /^([^#]+)(#(.*))?$/ ) 
            queue_base = $1
            task_name  = $3 || 'execute'
          end
  
          @client.send( "/queues/torquebox/tasks/#{queue_base}", 
                       :task=>task_name,
                       :payload=>payload )
        end
      end

    end
  end
end
