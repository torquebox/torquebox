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

require 'torquebox/messaging/destination'

module TorqueBox
  module Messaging
    module Backgroundable
      def self.included(base)
        base.extend(ClassMethods)
      end

      def background(options = { })
        BackgroundProxy.new(self, options)
      end

      module ClassMethods

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
          method = method.to_s
          if @__backgroundable_methods &&
              @__backgroundable_methods[method] &&
              !@__backgroundable_methods[method][:backgrounding]
            __enable_backgrounding(method)
          else
            super
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
        QUEUE_NAME = "/queues/torquebox/#{ENV['TORQUEBOX_APP_NAME']}/backgroundable"

        class << self
          def publish_message(receiver, method, args, options = { })
            Queue.new(QUEUE_NAME).publish({:receiver => receiver, :method => method, :args => args}, options)
          rescue javax.naming.NameNotFoundException => ex
            raise RuntimeError.new("The backgroundable queue is not available. Did you disable it by setting its concurrency to 0?")
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


