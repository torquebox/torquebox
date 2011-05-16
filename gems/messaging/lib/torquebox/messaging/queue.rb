require 'torquebox/messaging/destination'

module TorqueBox
  module Messaging
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
