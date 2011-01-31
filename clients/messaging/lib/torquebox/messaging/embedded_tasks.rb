require 'torquebox/messaging/destination'

module TorqueBox
  module Messaging
    module EmbeddedTasks
      def self.included(base)
        base.extend(ClassMethods)
      end

      def background
        BackgroundProxy.new(self)
      end

      module ClassMethods
        def always_background(*methods)
          methods.each do |method|
            async_method = "__async_#{method}"
            sync_method = "__sync_#{method}"
            define_method async_method do |*args|
              Util.publish_message(self, sync_method, args)
            end
            alias_method sync_method, method
            alias_method method, async_method
          end
        end
      end

      class BackgroundProxy
        def initialize(receiver)
          @receiver = receiver
        end

        def method_missing(method, *args, &block)
          @receiver.method_missing(method, *args, &block) unless @receiver.respond_to?(method)
          raise ArgumentError.new("Backgrounding a method with a block argument is not supported. If you need this feature, please file a feature request at http://issues.jboss.org/browse/TORQUE") if block_given?
          Util.publish_message(@receiver, method, args)
        end
      end
      
      module Util
        QUEUE_NAME = "/queues/torquebox/#{ENV['TORQUEBOX_APP_NAME']}/embedded-tasks"

        class << self
          def publish_message(receiver, method, args)
            Queue.new(QUEUE_NAME).publish(:receiver => receiver,
                                          :method => method,
                                          :args => args)
          end
        end
      end
    end
  end
end


