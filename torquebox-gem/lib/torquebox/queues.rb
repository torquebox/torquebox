
require 'org/torquebox/ruby/enterprise/client/client'

module TorqueBox

  module Queues


    def self.enqueue( queue_name, task_name, payload )
      TorqueBox::Client.connect() do |torquebox_client|
        camel_queue_name = queue_name.to_s.camelize
        destination_name = "#{torquebox_client.application_name}.#{camel_queue_name}"
        client = Java::OrgTorqueboxRubyEnterpriseQueues::RubyTaskQueueClient.new
        client.set_destination_name( destination_name )
        client.enqueue( task_name.to_s, payload )
      end
    end # enqueue(..)

  end # Queues
end # TorqueBox
