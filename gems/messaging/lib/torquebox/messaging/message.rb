
require 'base64'

module TorqueBox
  module Messaging
    class Message

      attr_reader :jms_message

      def initialize(jms_message, payload=nil)
        @jms_message = jms_message
        encode( payload ) if payload
      end

      def populate_message_headers(options)
        return if options.nil?
        options.each do |key, value|
          case key.to_s
          when 'correlation_id' then @jms_message.setJMSCorrelationID(value)
          when 'reply_to'       then @jms_message.setJMSReplyTo(value)
          when 'type'           then @jms_message.setJMSType(value)
          end
        end
      end
    
      def populate_message_properties(properties)
        return if properties.nil?
        properties.each do |key, value|
          case value
          when Integer
            @jms_message.set_long_property(key.to_s, value)
          when Float
            @jms_message.set_double_property(key.to_s, value)
          when TrueClass, FalseClass
            @jms_message.set_boolean_property(key.to_s, value)
          else
            @jms_message.set_string_property(key.to_s, value.to_s)
          end
        end
      end


      def encode(message)
        if message.is_a? String
          @jms_message.text = message
        else
          @jms_message.set_string_property( 'torquebox_encoding', 'base64' )
          marshalled = Marshal.dump( message )
          encoded = Base64.encode64( marshalled )
          @jms_message.text = encoded
        end
      end
    
      def decode()
        Message.decode( @jms_message )
      end

      def self.decode(jms_message)
        if jms_message.get_string_property( 'torquebox_encoding' ) == 'base64'
          serialized = Base64.decode64( jms_message.text )
          Marshal.restore( serialized )
        else
          jms_message.text
        end
      end

    end
  end
end
