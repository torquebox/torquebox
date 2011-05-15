
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

        def to_s 
          "[LiveDestination: connection_factory=#{connection_factory}, destination=#{destination}]"
        end

      end
    end
  end
end
