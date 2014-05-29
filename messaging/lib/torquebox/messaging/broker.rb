require 'torquebox/messaging/connection'
require 'torquebox/messaging/endpoint'

module TorqueBox
  module Messaging
    class Broker
      include TorqueBox::OptionUtils

      def self.find_or_create(name, options={})
        Broker.new(name, options)
      end

      def self.create_endpoint(name, options={})
        default_broker.create_endpoint(name, options)
      end

      def self.create_connection(options={})
        default_broker.create_connection(options)
      end

      def self.listen(endpoint, options={}, &block)
        default_broker.listen(endpoint, options, &block)
      end

      def self.subscribe(name, endpoint, selector=nil)
        default_broker.subscribe(name, endpoint, selector)
      end

      def self.publish(endpoint, message, options={})
        default_broker.publish(endpoint, message, options)
      end

      def self.receive(endpoint, options={})
        default_broker.receive(endpoint, options)
      end

      def self.request(endpoint, message, options={})
        default_broker.request(endpoint, message, options)
      end

      def self.respond(endpoint, options={}, &block)
        default_broker.respond(name, options, &block)
      end

      def create_endpoint(name, options={})
        validate_options(options, opts_to_set(WBMessaging::CreateEndpointOption))
        create_options = extract_options(options, WBMessaging::CreateEndpointOption)
        Endpoint.new(@internal_broker.find_or_create_endpoint(name, create_options))
      end

      def create_connection(options={})
        validate_options(options, opts_to_set(WBMessaging::CreateConnectionOption))
        create_options = extract_options(options, WBMessaging::CreateConnectionOption)
        Connection.new(@internal_broker.create_connection(create_options))
      end

      def with_connection(options, &block)
        connection = create_connection(options)
        begin
          block.call(connection)
        ensure
          connection.close
        end
      end

      def listen(endpoint, options={}, &block)
      end

      def subscribe(name, endpoint, selector=nil)
      end

      def publish(endpoint, message, options={})
        #TODO: validate options
        with_connection(options) do |connection|
          connection.publish(endpoint, message, options)
        end
      end

      def receive(endpoint, options={})
        #TODO: validate options
        with_connection(options) do |connection|
          connection.receive(endpoint, options)
        end
      end

      def request(endpoint, message, options={})

      end

      def respond(endpoint, options={}, &block)
      end

      def start
        @internal_broker.start
      end

      def stop
        @internal_broker.stop
      end

      protected

      WB = org.projectodd.wunderboss.WunderBoss
      WBMessaging = org.projectodd.wunderboss.messaging.Messaging

      def self.default_broker
        @broker ||= find_or_create('default')
      end

      def initialize(name, options={})
        @logger = WB.logger('TorqueBox::Messaging::Broker')
        validate_options(options, opts_to_set(WBMessaging::CreateOption))
        create_options = extract_options(options, WBMessaging::CreateOption)
        comp = WB.find_or_create_component(WBMessaging.java_class, name,
                                           create_options)
        @logger.debugf("TorqueBox::Messaging::Broker '%s' has component %s",
                       name, comp)
        @internal_broker = comp
        start
        at_exit { stop }
      end
    end
  end
end
