
module TorqueBox
  module Messaging
    class WebSocketsProcessor
    
      def initialize(params = {})
      end
    
      def close
        on_close
      end
      
      def on_message(data)
      end
    
      def send(data)
      end
      
      def on_close
        # default implementation; does nothing.
      end
    
    end
  end
end
