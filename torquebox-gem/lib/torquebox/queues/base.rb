
import org.torquebox.ruby.enterprise.queues.RubyTaskQueueClient
load 'org/torquebox/ruby/enterprise/client/client.rb'

puts "about to load TorqueBox::Queues::Base"

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
        puts "included into #{into}"

        class << into
          puts "defining enqueue on #{self}"
          def enqueue(task, payload=nil)
           
            puts "enqueue #{task} with #{payload}"
            TorqueBox::Client.connect() do |torquebox_client|
              puts "client #{torquebox_client}"
              destination_name = "#{torquebox_client.application_name}.#{self.name.gsub( /::/, '.' )}"
              puts "destination #{destination_name}"
              client = RubyTaskQueueClient.new
              client.set_destination_name( destination_name )
              client.enqueue( task.to_s, payload )
            end
          end
        end
      end

    end
  end
end

puts "done with load TorqueBox::Queues::Base"
