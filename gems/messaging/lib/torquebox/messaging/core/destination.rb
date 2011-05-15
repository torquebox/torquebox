require 'torquebox/messaging/core/destination'

module TorqueBox
  module Messaging
    module Core
      class Destination
        attr_reader :jms_destination
        def initialize(jms_destination)
          @jms_destination = jms_destination
        end
        def to_s
          "[Destination: jms_destination=#{jms_destination}]"
        end
      end
    end
  end
end
