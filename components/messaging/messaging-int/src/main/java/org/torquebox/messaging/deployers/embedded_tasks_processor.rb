require 'org.torquebox.torquebox-messaging-container'
require 'torquebox/messaging/message_processor'

module TorqueBox
  module Messaging
    class EmbeddedTasksProcessor < MessageProcessor
      def on_message(hash)
        hash[:receiver].send(hash[:method], *hash[:args])
      end
    end
  end
end
