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
  end
end
