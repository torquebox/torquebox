require 'torquebox/codecs'

module TorqueBox
  module Messaging
    # A read-only representation of a message.
    class Message
      attr_reader :internal_message

      def encoding
        Codecs.name_for_content_type(content_type)
      end

      def content_type
        @internal_message.content_type
      end

      def properties
        @internal_message.properties
      end

      def body
        @internal_message.body(Codecs.binary_content?(encoding) ?
                               Java::JavaClass.for_name("[B") :
                               java.lang.String.java_class)
      end

      def decode
        Codecs.decode(body, encoding)
      end

      protected

      def initialize(msg)
        @internal_message = msg
      end
    end
  end
end
