require 'torquebox/messaging/destination'

module TorqueBox
  module Messaging
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
