require 'torquebox/messaging/message_processor'

module TorqueBox
  module Messaging
    class WebSocketsProcessor < MessageProcessor
    
      attr :send_queue
    
      def initialize(params)
  	    @send_queue = TorqueBox::Messaging::Queue.new "/queues/websockets_#{params['applicationName']}_out"
      end
    
      def close
        on_close
      end
    
      def send(payload)
        begin
          @send_queue.publish(payload)
        rescue => e
          on_error(e)
        end
      end
      
      def on_close
        # default implementation; does nothing.
      end
    
    end
  end
end
