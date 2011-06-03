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

require 'torquebox/injectors'
require 'torquebox/messaging/session'
require 'torquebox/messaging/connection_factory'
require 'torquebox/messaging/ext/javax_jms_queue_browser'

module TorqueBox
  module Messaging
    class Destination
      include TorqueBox::Injectors
      include Enumerable

      
      attr_reader :connection_factory
      attr_reader :name
      attr_writer :enumerable_options
      
      PRIORITY_MAP = {
          :low => 1,
          :normal => 4,
          :high => 7,
          :critical => 9
      }

      def _dump(depth)
        return self.name.queue_name if self.name.respond_to?( :queue_name )
        self.name.to_s
      end

      def self._load(str)
       self.new( str )
      end

      def initialize(destination, connection_factory = __inject__( 'connection-factory' ))
        @name                = destination
        @connection_factory  = ConnectionFactory.new( connection_factory )
      end

      def publish(message, options = {})
        wait_for_destination(options[:startup_timeout]) do
          with_new_session do |session|
            session.publish self, message, normalize_options(options)
            session.commit if session.transacted?
          end
        end
      end

      def receive(options = {})
        wait_for_destination(options[:startup_timeout]) do
          with_new_session do |session|
            result = session.receive self, options
            session.commit if session.transacted?
            result
          end
        end
      end

      def each(&block)
        wait_for_destination do
          with_new_session do |session|
            destination = session.java_destination( self )
            browser = session.create_browser( destination, enumerable_options[:selector] )
            begin
              browser.each(&block)
            ensure
              browser.close
            end
          end
        end
      end

      def with_new_session(transacted=true, ack_mode=Session::AUTO_ACK, &block)
        result = nil
        connection_factory.with_new_connection do |connection|
          connection.with_new_session( transacted, ack_mode ) do |session|
            result = block.call(session)
          end
        end
        result
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

      def enumerable_options
        @enumerable_options ||= { }
        @enumerable_options
      end
      
      def to_s 
        name
      end

    end
  end
end
