require 'torquebox/messaging/destination'
require 'org.torquebox.torquebox-messaging-client'
require 'org.torquebox.torquebox-messaging-container'
require 'torquebox/messaging/message_processor'

module TorqueBox
  module Messaging
    module EmbeddedTasks
      
      def handle_async(*methods)
        methods.each do |method|
          async_method = "__async_#{method}"
          sync_method = "__sync_#{method}"
          define_method async_method do |*args|
            Util.publish_message(self, sync_method, args)
          end
          alias_method sync_method, method
          alias_method method, async_method
        end
      end

      module Util
        QUEUE_NAME = "/queues/torquebox/#{ENV['TORQUEBOX_APP_NAME']}/embedded-tasks"
        
        class << self
          def publish_message(receiver, method, args)
            Queue.new(QUEUE_NAME).publish(:receiver => receiver,
                                          :method => method,
                                          :args => args)
          end
        end
        
      end
    end
  end
end


