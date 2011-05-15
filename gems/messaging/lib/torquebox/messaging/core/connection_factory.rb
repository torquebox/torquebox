
module TorqueBox
  module Messaging
    module Core
      class ConnectionFactory

        attr_reader :jms_connection_factory

        def initialize(jms_connection_factory)
          @jms_connection_factory = jms_connection_factory
        end

        def to_s
          "[ConnectionFactory: jms_connection_factory=#{jms_connection_factory}]"
        end

      end
    end
  end
end
