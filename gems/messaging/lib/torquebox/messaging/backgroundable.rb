# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

require 'torquebox/messaging/queue'
require 'torquebox/messaging/future'
require 'torquebox/messaging/task'
require 'torquebox/messaging/future_status'
require 'torquebox/injectors'

module TorqueBox
  module Messaging

    # Backgroundable provides mechanism for executing an object's
    # methods asynchronously.
    module Backgroundable

      def self.included(base)
        base.extend(ClassMethods)
        base.send(:include, FutureStatus)
      end

      # Allows you to background any method that has not been marked
      # as a backgrounded method via {ClassMethods#always_background}.
      # @param [Hash] options that are passed through to
      #   {TorqueBox::Messaging::Destination#publish}
      # @return [Future]
      def background(options = { })
        BackgroundProxy.new(self, options)
      end

      module ClassMethods

        # Marks methods to always be backgrounded. Takes one or more
        # method symbols, and an optional options hash as the final
        # argument. The options allow you to set publish options for
        # each call.
        # see TorqueBox::Messaging::Destination#publish
        def always_background(*methods)
          options = methods.last.is_a?(Hash) ? methods.pop : {}
          @__backgroundable_methods ||= {}

          methods.each do |method|
            method = method.to_s
            if !@__backgroundable_methods[method]
              @__backgroundable_methods[method] ||= { }
              @__backgroundable_methods[method][:options] = options
              if Util.instance_methods_include?(self, method) ||
                  Util.private_instance_methods_include?(self, method)
                __enable_backgrounding(method)
              end
            end
          end
        end

        def method_added(method)
          super
          method = method.to_s
          if @__backgroundable_methods &&
              @__backgroundable_methods[method] &&
              !@__backgroundable_methods[method][:backgrounding]
            __enable_backgrounding(method)
          end
        end

        def __enable_backgroundable_newrelic_tracing(method)
          if defined?(NewRelic::Agent)
            include(NewRelic::Agent::Instrumentation::ControllerInstrumentation) unless
              include?(NewRelic::Agent::Instrumentation::ControllerInstrumentation)
            begin
              add_transaction_tracer(method, :name => method.sub("__sync_", ""), :category => :task)
            rescue Exception => e
              log.error "Error loading New Relic for backgrounded method #{method.sub("__sync_", "")}: #{e}"
            end
          end
        end

        private
        def __enable_backgrounding(method)
          privatize = Util.private_instance_methods_include?(self, method)
          protect = Util.protected_instance_methods_include?(self, method) unless privatize
          async_method = "__async_#{method}"
          sync_method = "__sync_#{method}"

          @__backgroundable_methods[method][:backgrounding] = true
          options = @__backgroundable_methods[method][:options]
          class_eval do

            define_method async_method do |*args|
              Util.publish_message(self, sync_method, args, options)
            end

            alias_method sync_method, method
            alias_method method, async_method

            if privatize || protect
              send((privatize ? :private : :protected), method, sync_method, async_method)
            end

          end
        ensure
          @__backgroundable_methods[method][:backgrounding] = nil
        end

      end

      class BackgroundProxy
        def initialize(receiver, options)
          @receiver = receiver
          @options = options
        end

        def method_missing(method, *args, &block)
          @receiver.method_missing(method, *args, &block) unless @receiver.respond_to?(method)
          raise ArgumentError.new("Backgrounding a method with a block argument is not supported. If you need this feature, please file a feature request at http://issues.jboss.org/browse/TORQUE") if block_given?
          Util.publish_message(@receiver, method, args, @options)
        end
      end

      module Util
        extend TorqueBox::Injectors
        
        class << self
          def publish_message(receiver, method, args, options = { })
            queue_name = Task.queue_name( "torquebox_backgroundable" )
            queue = Queue.new( queue_name )
            future = Future.new( queue )
            options[:encoding] = :marshal
            queue.publish( {:receiver => receiver,
                             :future_id => future.correlation_id,
                             :future_queue => queue_name,
                             :method => method,
                             :args => args}, options )
            
            future
          rescue javax.jms.InvalidDestinationException => ex
            raise RuntimeError.new("The Backgroundable queue is not available. Did you disable it by setting its concurrency to 0?")
          end

          def instance_methods_include?(klass, method)
            methods_include?(klass.instance_methods, method)
          end

          def private_instance_methods_include?(klass, method)
            methods_include?(klass.private_instance_methods, method)
          end

          def protected_instance_methods_include?(klass, method)
            methods_include?(klass.protected_instance_methods, method)
          end

          def methods_include?(methods, method)
            method = (RUBY_VERSION =~ /^1\.9\./ ? method.to_sym : method.to_s)
            methods.include?(method)
          end
        end
      end
    end
  end
end


