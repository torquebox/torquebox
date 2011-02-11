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

require 'org/torquebox/interp/core/kernel'
require 'torquebox/messaging/client'

module TorqueBox
  module Messaging

    module Destination
      attr_reader :name

      PRIORITY_MAP = {
          :low => 1,
          :normal => 4,
          :high => 7,
          :critical => 9
      }
      
      def initialize(name, options={})
        @name = name
        @connect_options = options
      end
      
      def publish(message, options = {})
        Client.connect(@connect_options) do |session|
          session.publish name, message, normalize_options(options)
          session.commit if session.transacted?
        end
      end

      def receive(options={})
        result = nil
        Client.connect(@connect_options) do |session|
          result = session.receive( name, options )
          session.commit if session.transacted?
        end
        result
      end

      def start
        TorqueBox::Kernel.lookup("JMSServerManager") do |server|
          destination.name = name
          destination.server = server
          destination.start
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
    end

    class Queue
      include Destination
      def destination
        @destination ||= Java::org.torquebox.messaging.core::ManagedQueue.new
      end

      def publish_and_receive(message, options={})
        result = nil
        Client.connect(@connect_options) do |session|
          result = session.send_and_receive(name, message, options)
          session.commit if session.transacted?
        end
        result
      end

      def receive_and_publish(options={})
        request = receive(options.merge(:decode => false))
        unless request.nil?
          reply_to = request.jmsreply_to
          request_payload = request.decode
          response = block_given? ? yield(request_payload) : request_payload

          Client.connect(@connect_options) do |session|
            session.publish(reply_to, response)
            session.commit if session.transacted?
          end
        end
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
