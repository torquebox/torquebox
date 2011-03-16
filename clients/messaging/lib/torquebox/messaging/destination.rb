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

require 'torquebox/kernel'
require 'torquebox/messaging/client'

require 'torquebox/messaging/ext/javax_jms_queue_browser'

module TorqueBox
  module Messaging

    module Destination
      include Enumerable
      
      attr_reader :name

      PRIORITY_MAP = {
          :low => 1,
          :normal => 4,
          :high => 7,
          :critical => 9
      }

      def initialize(name, connect_options=nil, enumerable_options=nil)
        @name = name
        @connect_options = connect_options || {}
        @enumerable_options = enumerable_options || {}
      end

      def publish(message, options = {})
        wait_for_destination(options[:startup_timeout]) {
          Client.connect(@connect_options) do |session|
            session.publish name, message, normalize_options(options)
            session.commit if session.transacted?
          end
        }
      end

      def receive(options={})
        result = nil
        wait_for_destination(options[:startup_timeout]) {
          Client.connect(@connect_options) do |session|
            result = session.receive( name, options )
            session.commit if session.transacted?
          end
        }
        result
      end

      def start
        TorqueBox::Kernel.lookup("JMSServerManager") do |server|
          destination.name = name
          destination.server = server
          destination.create
        end
      end
      alias_method :create, :start

      def destroy
        TorqueBox::Kernel.lookup("JMSServerManager") do |server|
          destination.name = name
          destination.server = server
          destination.destroy
        end
      end

      def each(&block)
        wait_for_destination do
          Client.connect(@connect_options) do |session|
            destination = session.lookup_destination( name )
            browser = session.create_browser( destination, @enumerable_options[:selector] )
            begin
              browser.each(&block)
            ensure
              browser.close
            end
          end
        end
      end
      
      def to_s
        name
      end

      protected

      def normalize_options(options)
        if options.has_key?(:persistent)
          options[:delivery_mode] =
            options.delete(:persistent) ? javax.jms::DeliveryMode.PERSISTENT : javax.jms::DeliveryMode.NON_PERSISTENT
        end

        if options.has_key?(:priority)
          if PRIORITY_MAP[options[:priority]]
            options[:priority] = PRIORITY_MAP[options[:priority]]
          elsif (0..9) === options[:priority].to_i
            options[:priority] = options[:priority].to_i
          else
            raise ArgumentError.new(":priority must in the range 0..9, or one of #{PRIORITY_MAP.keys.collect {|k| ":#{k}"}.join(',')}")
          end
        end

        options
      end

      def wait_for_destination(timeout=nil, &block)
        timeout ||= 30000 # 30s default
        start = Time.now
        begin
          block.call
        rescue javax.naming.NameNotFoundException => ex
          elapsed = (Time.now - start) * 1000
          if elapsed > timeout
            raise ex
          else
            sleep(0.1)
            retry
          end
        end
      end
    end

    class Queue
      include Destination
      def destination
        @destination ||= Java::org.torquebox.messaging.core::ManagedQueue.new
      end

      def publish_and_receive(message, options={})
        result = nil
        wait_for_destination(options[:startup_timeout]) {
          Client.connect(@connect_options) do |session|
            result = session.publish_and_receive(name, message,
                                                 normalize_options(options))
            session.commit if session.transacted?
          end
        }
        result
      end

      def receive_and_publish(options={}, &block)
        wait_for_destination(options[:startup_timeout]) {
          Client.connect(@connect_options) do |session|
            session.receive_and_publish(name, normalize_options(options), &block)
            session.commit if session.transacted?
          end
        }
      end
    end

    class Topic
      include Destination
      def destination
        @destination ||= Java::org.torquebox.messaging.core::ManagedTopic.new
      end
    end

  end
end
