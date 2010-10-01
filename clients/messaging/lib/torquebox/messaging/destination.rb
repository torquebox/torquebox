require 'org/torquebox/interp/core/kernel'
require 'torquebox/messaging/client'

module TorqueBox
  module Messaging

    module Destination
      attr_reader :name

      def initialize name
        @name = name
      end
      
      def publish message, options={}
        Client.connect(options) do |session|
          session.publish name, message
          session.commit if session.transacted?
        end
      end

      def receive options={}
        result = nil
        Client.connect(options) do |session|
          result = session.receive( name, options )
          session.commit if session.transacted?
        end
        result
      end

      def start
        TorqueBox::Kernel.lookup("JMSServerManager") do |server|
          destination.name = name
          destination.server = server
          destination.start
        end
      end

      def destroy
        TorqueBox::Kernel.lookup("JMSServerManager") do |server|
          destination.name = name
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
