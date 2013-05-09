# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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
require 'torquebox/msc'
require 'torquebox/messaging/session'
require 'torquebox/messaging/connection_factory'
require 'torquebox/messaging/ext/javax_jms_queue_browser'

module TorqueBox
  module Messaging
    class Destination
      include Enumerable

      attr_reader :connection_factory
      attr_reader :name
      attr_reader :java_destination
      attr_accessor :enumerable_options
      attr_accessor :connect_options

      PRIORITY_MAP = {
        :low => 1,
        :normal => 4,
        :high => 7,
        :critical => 9
      }

      def _dump(level)
        to_s
      end

      def self._load(str)
        self.new( str )
      end

      def initialize(destination, connection_factory_or_options = nil)
        raise ArgumentError, "destination cannot be nil" unless destination
        if connection_factory_or_options.nil? || connection_factory_or_options.is_a?( Hash )
          options = connection_factory_or_options
          connection_factory = TorqueBox.fetch( 'connection-factory' )
          unless options.nil?
            # Don't use our internal connection factory if the user
            # has specified a host or port to connect to
            connection_factory = nil if options[:host] or options[:port]
          end
          @connection_factory = ConnectionFactory.new( connection_factory )
          @connect_options = options || {}
        else
          @connection_factory  = ConnectionFactory.new( connection_factory_or_options )
          @connect_options = {}
        end


        if destination.is_a?(javax.jms.Destination )
          if destination.is_a?(javax.jms.Queue)
            @name = destination.queue_name
          else
            @name = destination.topic_name
          end

          @java_destination = destination
        else
          @name = destination
        end


        @enumerable_options  = {}
      end

      # Stops the destination.
      #
      # @note This is an asynchronous method.
      # @return [java.util.concurrent.CountDownLatch] The latch to wait for the task completion
      # @see http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CountDownLatch.html
      def stop
        TorqueBox::Messaging::Destination.with_destinationizer do |destinationizer|
          destinationizer.remove_destination(name)
        end
      end

      # Stops the destination.
      #
      # @note This is a synchronous method.
      # @return [Boolean] true if the destination was successfully stopped, false otherwise
      # @see TorqueBox::Messaging::Destination.stop
      def stop_sync
        latch = stop
        TorqueBox::Messaging::Destination.wait_for_latch(latch)
      end

      # Publishes a message to the destination
      #
      # @param message The message to publish
      # @param options Optional parameters (a Hash)
      # @return [void]
      def publish(message, options = {})
        wait_for_destination(options[:startup_timeout]) do
          with_session(options) do |session|
            session.publish self, message, normalize_options(options)
          end
        end
      end

      def receive(options = {}, &block)
        wait_for_destination(options[:startup_timeout]) do
          func = lambda do
            with_session(options) do |session|
              session.receive self, options, &block
            end
          end
          if block
            TorqueBox.transaction &func
          else
            func.call
          end
        end
      end

      def each(&block)
        wait_for_destination do
          with_session do |session|
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

      def with_session(opts = {})
        transactional = opts.fetch(:tx, true)

        # https://issues.jboss.org/browse/TORQUE-1033
        # If there is no encoding for the message, set the default one
        # for the destination. If the encoding for destination isn't set
        # it won't hurt
        opts[:encoding] = @connect_options[:encoding] if opts[:encoding].nil?

        connection_factory.with_new_connection(connect_options, transactional) do |connection|
          connection.with_session do |session|
            yield session
          end
        end
      end

      def wait_for_destination(timeout=nil, &block)
        timeout ||= 30_000 # 30s default
        start = Time.now
        begin
          block.call
        rescue javax.naming.NameNotFoundException, javax.jms.JMSException
          elapsed = (Time.now - start) * 1000
          if elapsed > timeout
            raise
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

      class << self

        # List all destinations of this application.
        #
        # @return Array of {TorqueBox::Messaging::Queue} or {TorqueBox::Messaging::Topic}
        #  depending on the destination type.
        def list
          # Get the JMS Manager
          TorqueBox::ServiceRegistry.lookup("jboss.messaging.default.jms.manager") do |manager|

            # JMSServerControl will let us grab the deployed queue/topic list
            server_control = Java::org.hornetq.jms.management.impl.JMSServerControlImpl.new(manager)

            # Retrieve the destination list appropriate to the destination type
            if self == TorqueBox::Messaging::Topic
              names = server_control.topic_names
            elsif self == TorqueBox::Messaging::Queue
              names = server_control.queue_names
            else
              names = []
            end

            names.map { |name| self.new(name) }
          end
        end

        # Lookup a destination of this application by name. A destination could be
        # a queue or topic.
        #
        # @param [String] name of the destination
        #
        # @return [TorqueBox::Messaging::Queue] or [TorqueBox::Messaging::Topic]
        #   The destination instance.
        def lookup(name)
          list.find { |destination| destination.name == name }
        end

        # @api private
        def with_destinationizer
          service_name = TorqueBox::MSC.deployment_unit.service_name.append('torquebox').append('messaging').append('destinationizer')

          TorqueBox::ServiceRegistry.lookup(service_name) do |destinationizer|
            yield destinationizer
          end
        end

        # @api private
        def wait_for_latch(latch)
          begin
            # Wait for the services to come up for up to 30 seconds
            latch.await(45, java.util.concurrent.TimeUnit::SECONDS)
          rescue
            return false
          end

          true
        end
      end

    end
  end
end
