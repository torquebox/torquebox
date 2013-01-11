# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

module TorqueBox
  module Messaging
    class Message

      attr_reader :jms_message

      # if no encoding specified in the message itself assume the legacy encoding
      DEFAULT_DECODE_ENCODING = :marshal_base64
      
      # if no encoding specified when creating a message and no global
      # defaut set use :marshal
      DEFAULT_ENCODE_ENCODING = :marshal
      
      ENCODING_PROPERTY = "__ContentEncoding__"

      def initialize(jms_session, payload)
        @jms_message = self.class::JMS_TYPE == :text ? jms_session.create_text_message :
          jms_session.create_bytes_message
        set_encoding
        encode( payload )
      end

      def initialize_from_message(jms_message)
        @jms_message = jms_message
      end

      def set_encoding
        @jms_message.set_string_property( ENCODING_PROPERTY, self.class::ENCODING.to_s )
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
        return if properties.nil? or properties.empty?
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

      def method_missing(*args)
        @jms_message.send(*args)
      end

      def respond_to?(symbol, include_private = false)
        super || @jms_message.respond_to?(symbol, include_private)
      end

      class << self
        alias :__new__ :new

        def inherited(subclass)
          class << subclass
            alias :new :__new__
          end
        end

        def new(jms_message_or_session, payload = nil, encoding = nil)
          if jms_message_or_session.is_a?( javax.jms::Session )
            encoding ||= ENV['DEFAULT_MESSAGE_ENCODING'] || DEFAULT_ENCODE_ENCODING
            klass = class_for_encoding( encoding.to_sym )
            klass.new( jms_message_or_session, payload )
          else
            encoding = extract_encoding_from_message( jms_message_or_session ) || DEFAULT_DECODE_ENCODING
            klass = class_for_encoding( encoding )
            msg = klass.allocate
            msg.initialize_from_message( jms_message_or_session )
            msg
          end
        end

        def encoding_map
          @encoding_map ||= { }
        end

        def register_encoding(klass)
          encoding_map[klass::ENCODING] = klass
        end

        def class_for_encoding(encoding)
          klass = encoding_map[encoding.to_sym]
          raise ArgumentError.new( "No message class found for encoding '#{encoding}'" ) unless klass
          klass
        end

        def extract_encoding_from_message(jms_message)
          jms_message.get_string_property( ENCODING_PROPERTY )
        end

      end
    end
  end
end
