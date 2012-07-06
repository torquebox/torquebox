module TorqueBox
  module Messaging
    module Backgroundable
      def self.included(base)
        base.extend(ClassMethods)
      end

      def background(options = {})
        self
      end

      module ClassMethods
        def always_background(*methods)
          # no-op
        end
      end
    end

    class MessageProcessor
    end

    class Queue
      def initialize(*args)
        # no-op
      end

      def publish(*args)
        # no-op
      end

      def receive(*args)
        # no-op
      end
    end

    class Message
      class <<self
        def register_encoding(klass)
          # no-op
        end
      end
    end
  end
end
