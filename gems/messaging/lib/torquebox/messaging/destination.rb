require 'torquebox/injectors'
require 'torquebox/messaging/session'
require 'torquebox/messaging/connection_factory'

module TorqueBox
  module Messaging
    class Destination

      include TorqueBox::Injectors

      attr_reader :connection_factory
      attr_reader :name
      
      PRIORITY_MAP = {
          :low => 1,
          :normal => 4,
          :high => 7,
          :critical => 9
      }

      def initialize(destination, connection_factory = inject( 'connection-factory' ))
        @name                = destination
        @connection_factory  = ConnectionFactory.new( connection_factory )
      end

      def publish(message, options = {})
        with_new_session do |session|
          session.publish self, message, normalize_options(options)
          session.commit if session.transacted?
        end
      end

      def receive(options = {})
        with_new_session do |session|
          result = session.receive self, options
          session.commit if session.transacted?
          result
        end
      end

      def with_new_session(transacted=true, ack_mode=Session::AUTO_ACK, &block)
        result = nil
        connection_factory.with_new_connection do |connection|
          connection.with_new_session( transacted, ack_mode ) do |session|
            result = block.call(session)
          end
        end
        
        result
      end
      
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

        options
      end

      def to_s 
        name
      end

    end
  end
end
