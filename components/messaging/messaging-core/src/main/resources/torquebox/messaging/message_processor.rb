require 'torquebox/messaging/javax_jms_text_message'

module TorqueBox
  module Messaging
    class MessageProcessor

      attr_accessor :message

      def initialize
        @message = nil 
      end
      
      def on_message(body)
        throw "Your subclass must implement on_message(body)"
      end

      def on_error(error)
        raise error
      end

      def process!(message)
        @message = message
        begin
          on_message( message.decode )
        rescue Exception => e
          on_error( e ) 
        end 
      end

    end
  end
end
