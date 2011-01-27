require 'org/torquebox/interp/core/kernel'
require 'torquebox/messaging/client'

module TorqueBox
  module Messaging

    module Destination
      attr_reader :name

      def initialize name, options={}
        @name = name
        @options = options
      end
      
      def publish message, options={}
        Client.connect(@options.merge(options)) do |session|
          session.publish name, message
          session.commit if session.transacted?
        end
      end

      def receive options={}
        result = nil
        Client.connect(@options.merge(options)) do |session|
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
      
      def to_s
        name
      end
    end

    class Queue
      include Destination
      def destination
        @destination ||= Java::org.torquebox.messaging.core::ManagedQueue.new
      end

      def publish_and_receive message, options={}
        result = nil
        Client.connect(@options.merge(options)) do |session|
          result = session.send_and_receive(name, message, options)
          session.commit if session.transacted?
        end
        result
      end

      def receive_and_publish options={}
        request = receive(options.merge(:decode => false))
        unless request.nil?
          reply_to = request.jmsreply_to
          request_payload = request.decode
          response = block_given? ? yield(request_payload) : request_payload

          Client.connect(@options.merge(options)) do |session|
            session.publish(reply_to, response)
            session.commit if session.transacted?
          end
        end
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
