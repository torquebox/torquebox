# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

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
        marshalled = Marshal.dump( message )
        encoded = Base64.encode64( marshalled )
        @jms_message.text = encoded
      end
    
      def decode()
        Message.decode( @jms_message )
      end

      def self.decode(jms_message)
        serialized = Base64.decode64( jms_message.text )
        Marshal.restore( serialized )
      end

      def method_missing(*args)
        @jms_message.send(*args)
      end

    end
  end
end
