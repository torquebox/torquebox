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
            @__backgroundable_methods[method] = options
            if instance_methods.include?(method) || private_instance_methods.include?(method)
              __enable_backgrounding(method, options)
            end
          end
        end

        def method_added(method)
          # don't enable backgrounding for methods we don't care about
          # and ignore adding of the method we are currently
          # backgrounding. This fixes class reloading that just
          # redef's methods [TORQUE-260]
          if @__backgroundable_methods &&
              @__backgroundable_methods[method.to_s] &&
              @__currently_backgrounding_method != method
            __enable_backgrounding(method, @__backgroundable_methods[method.to_s])
          else
            super
          end
        end

        def __enable_backgrounding(method, options)
          privatize = private_instance_methods.include?(method.to_s)
          protect = protected_instance_methods.include?(method.to_s) unless privatize

          async_method = "__async_#{method}"
          sync_method = "__sync_#{method}"

          # stash the method we are currently working on to turn off
          # method_added for the alias_method call.
          @__currently_backgrounding_method = method
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
          @__currently_backgrounding_method = nil
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
          end

        end
      end
    end
  end
end


