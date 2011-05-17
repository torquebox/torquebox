require 'torquebox/messaging/destination'
require 'torquebox/messaging/connection_factory'

module TorqueBox
  module Messaging
    class Topic < Destination

      def to_s
        "[Topic: #{super}]"
      end
    end
  end
end
