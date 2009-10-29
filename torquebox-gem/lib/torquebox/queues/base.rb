
#import org.torquebox.ruby.enterprise.queues.RubyTaskQueueClient
require 'org/torquebox/ruby/enterprise/client/client'

module TorqueBox
  module Queues

    module Base

      def log
        @logger
      end

      def log=(logger)
        @logger = logger
      end

      def self.included(into)

        class << into
          def enqueue(task, payload=nil)
           
            TorqueBox::Client.connect() do |torquebox_client|
              destination_name = "#{torquebox_client.application_name}.#{self.name.gsub( /::/, '.' )}"
              client = Java::OrgTorqueboxRubyEnterpriseQueues::RubyTaskQueueClient.new
              client.set_destination_name( destination_name )
              client.enqueue( task.to_s, payload )
            end
          end
        end
      end

    end
  end
end
