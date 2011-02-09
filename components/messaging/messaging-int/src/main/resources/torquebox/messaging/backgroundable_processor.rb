require 'torquebox/messaging/message_processor'
require 'torquebox/messaging/const_missing'

module TorqueBox
  module Messaging
    class BackgroundableProcessor < MessageProcessor
      def on_message(hash)
        hash[:receiver].send(hash[:method], *hash[:args])
      end
    end
  end
end
