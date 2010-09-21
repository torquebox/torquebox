require 'org/torquebox/interp/core/kernel'

module TorqueBox
  module Messaging

    module Destination
      def initialize name
        destination.name = name
      end
      
      def name
        destination.name
      end

      def start
        TorqueBox::Kernel.lookup("JMSServerManager") do |server|
          destination.server = server
          destination.start
        end
      end

      def destroy
        TorqueBox::Kernel.lookup("JMSServerManager") do |server|
          destination.server = server
          destination.destroy
        end
      end
    end

    class Queue
      include Destination
      def destination
        @destination ||= Java::org.torquebox.messaging.core::ManagedQueue.new
      end
    end
    
    class Topic
      include Destination
      def destination
        @destination ||= Java::org.torquebox.messaging.core::ManagedTopic.new
      end
    end

  end
end
