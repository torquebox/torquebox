require 'torquebox/messaging/helpers'

module TorqueBox
  module Messaging
    # Represents a connection to the message broker.
    #
    # Most messaging operations default to a shared, internal
    # connection. You should only need to create a Connection directly
    # if you are connecting to a remote broker.
    class Connection
      include TorqueBox::OptionUtils
      include TorqueBox::Messaging::Helpers
      extend TorqueBox::OptionUtils

      attr_reader :internal_connection

      # Valid options for Connection creation.
      CONNECTION_OPTIONS = optset(WBMessaging::CreateConnectionOption)

      # Creates a new connection.
      #
      # You are responsible for closing any connections you create.
      #
      # If given a block, the Connection instance will be passed to
      # the block and the Connection will be closed once the block
      # returns.
      #
      # @param options [Hash]
      # @option options :client_id [String] Identifies the client id
      #   for use with a durable topic subscriber.
      # @option options :host [String] The host of a remote broker.
      # @option options :port [Number] (nil, 5445 if :host provided)
      #   The port of a remote broker.
      # @return [Connection]
      def initialize(options={}, &block)
        validate_options(options, CONNECTION_OPTIONS)
        create_options = extract_options(options, WBMessaging::CreateConnectionOption)
        @internal_connection = default_broker.create_connection(create_options)
        if block
          begin
            block.call(self)
          ensure
            close
          end
        end
      end

      # Creates a Session from this connection.
      #
      # If given a block, the Sesson instance will be passed to
      # the block and the Session will be closed once the block
      # returns.
      #
      # @param (see Session#initialize)
      # @return (see Session#initialize)
      def create_session(mode=TorqueBox::Messaging::Session::DEFAULT_MODE, &block)
        TorqueBox::Messaging::Session.new(mode, self, &block)
      end

      # Creates a Queue from this connection.
      #
      # The Queue instance will use this connection for
      # any of its methods that take a `:connection` option.
      #
      # @param (see Queue#initialize)
      # @return (see Queue#initialize)
      def queue(name, options={})
        TorqueBox::Messaging::Queue.new(name, options.merge(connection: self))
      end

      # Creates a Topic from this connection.
      #
      # The Topic instance will use this connection for
      # any of its methods that take a `:connection` option.
      #
      # @param (see Topic#initialize)
      # @return (see Topic#initialize)
      def topic(name, options={})
        TorqueBox::Messaging::Topic.new(name, options.merge(connection: self))
      end

      # Closes this connection.
      #
      # Once closed, any operations on this connection will raise
      # errors.
      # @return [void]
      def close
        @internal_connection.close
      end

    end
  end
end
