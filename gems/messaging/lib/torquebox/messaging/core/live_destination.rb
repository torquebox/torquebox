
module TorqueBox
  module Messaging
    module Core
      class LiveDestination

        attr_reader :connection_factory
        attr_reader :destination

        def initialize(connection_factory, destination)
          @connection_factory = connection_factory
          @destination        = destination
        end

  
        def publish(message, options = {})
          with_new_session do |session|
            session.publish name, message, normalize_options(options)
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

        def to_s 
          "[LiveDestination: connection_factory=#{connection_factory}, destination=#{destination}]"
        end

      end
    end
  end
end
