require 'torquebox/messaging/core/destination'

module TorqueBox
  module Messaging
    module Core
      class Queue < Destination
        def initialize(jms_queue)
          super
        end

        def to_s
          "[Queue: #{super}]"
        end

      end
    end
  end
end
