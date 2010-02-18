require 'torquebox-messaging-client'

module TorqueBox
  module Messaging
    module Tasks
      def self.enqueue(task_spec, payload={})
        if ( task_spec =~ /^([^#]+)(#(.*))?$/ ) 
          queue_base = $1
          task_name  = $3 || 'execute'
          TorqueBox::Messaging::Client.connect do |client|
            client.send( "/queues/torquebox/tasks/#{queue_base}", 
                         :task=>task_name,
                         :payload=>payload )
          end
        end
      end
    end
  end
end
