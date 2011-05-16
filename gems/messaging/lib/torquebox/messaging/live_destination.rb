
module TorqueBox
  module Messaging
    class LiveDestination

      attr_reader :connection_factory
      attr_reader :destination

      def initialize(connection_factory, destination)
        @connection_factory = connection_factory
        @destination        = destination
      end


      def publish(message, options = {})
        with_new_session do |session|
          session.publish destination, message, normalize_options(options)
          session.commit if session.transacted?
        end
      end

      def with_new_session(transacted=true, ack_mode=Session::AUTO_ACK, &block)
        connection_factory.with_new_connection do |connection|
          connection.with_new_session do |session|
            result = block.call(session)
          end
        end
        return result
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
        destination.to_s
      end

    end
  end
end
