require 'torquebox/messaging/core/destination'

module TorqueBox
  module Messaging
    module Core
      class Topic < Destination
        def initialize(jms_topic)
          super
        end

        def to_s
          "[Topic: #{super}]"
        end
      end
    end
  end
end
