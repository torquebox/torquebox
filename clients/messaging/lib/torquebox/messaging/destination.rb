require 'org/torquebox/interp/core/kernel'
require 'torquebox/messaging/client'

module TorqueBox
  module Messaging

    module Destination
      attr_reader :name

      PRIORITY_MAP = {
          :low => 1,
          :normal => 4,
          :high => 7,
          :critical => 9
      }
      
      def initialize(name, options={})
        @name = name
        @connect_options = options
      end
      
      def publish(message, options = {})
        Client.connect(@connect_options) do |session|
          session.publish name, message, normalize_options(options)
          session.commit if session.transacted?
        end
      end

      def receive(options={})
        result = nil
        Client.connect(@connect_options) do |session|
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
      alias_method :create, :start
      
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

      protected
      
      def normalize_options(options)
        if options.has_key?(:persistent)
          options[:delivery_mode] =
            options.delete(:persistent) ? javax.jms::DeliveryMode.PERSISTENT : javax.jms::DeliveryMode.NON_PERSISTENT
        end
        
        if options.has_key?(:priority)
          if PRIORITY_MAP[options[:priority]]
            options[:priority] = PRIORITY_MAP[options[:priority]]
          elsif (0..9) === options[:priority].to_i
            options[:priority] = options[:priority].to_i
          else
            raise ArgumentError.new(":priority must in the range 0..9, or one of #{PRIORITY_MAP.keys.collect {|k| ":#{k}"}.join(',')}")
          end
        end

        options[:ttl] *= 1000 if options[:ttl]
        
        options
      end
    end

    class Queue
      include Destination
      def destination
        @destination ||= Java::org.torquebox.messaging.core::ManagedQueue.new
      end

      def publish_and_receive(message, options={})
        result = nil
        Client.connect(@connect_options) do |session|
          result = session.send_and_receive(name, message, options)
          session.commit if session.transacted?
        end
        result
      end

      def receive_and_publish(options={})
        request = receive(options.merge(:decode => false))
        unless request.nil?
          reply_to = request.jmsreply_to
          request_payload = request.decode
          response = block_given? ? yield(request_payload) : request_payload

          Client.connect(@connect_options) do |session|
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
