
module TorqueBox
  module Messaging
    class Processor

      attr_accessor :message

      def initialize
        @message = nil 
      end
      
      def on_message(body)
        throw "Your subclass must implement on_message(body)"
      end

      def on_error(error)
        puts error.message
        puts error.backtrace        
      end

      def process!(message)
        @message = message
        begin
          on_message( message.text )
        rescue => e
          on_error( e ) 
        end 
      end

    end
  end
end
