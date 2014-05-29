module TorqueBox
  module Messaging
    # Represents a session with the message broker.
    #
    # If you need transactional semantics, you will need to manage
    # your own sessions. You can also achieve better perfomance when
    # doing lots of {Destination#publish} or {Destination#receive}
    # calls in rapid succession by managing your own session, since
    # each call will create and close a session if one is not
    # provided.
    class Session
      include TorqueBox::OptionUtils
      include TorqueBox::Messaging::Helpers
      extend TorqueBox::OptionUtils

      # @api private
      attr_reader :internal_session

      DEFAULT_MODE = :auto_ack

      # Creates a new connection.
      #
      # You are responsible for closing any sessions you create.
      #
      # If given a block, the Session instance will be passed to
      # the block and the Session will be closed once the block
      # returns.
      #
      # If no connection is provided, the default connection is used.
      #
      # @param mode [Symbol] (:auto_ack) One of: :auto_ack,
      #   :client_ack, :transacted
      # @param connection [Connection] A connection to use, caller
      #   expected to close.
      # @return [Session]
      def initialize(mode=DEFAULT_MODE, connection=nil, &block)
        create_options = extract_options({mode: coerce_mode(mode)},
                                         WBConnection::CreateSessionOption)
        internal_connection = connection ? connection.internal_connection :
          default_broker.default_connection
        @internal_session = internal_connection.create_session(create_options)
        if block
          begin
            block.call(self)
          ensure
            close
          end
        end
      end

      # Rolls back the session.
      #
      # This only has affect for :transacted sessions.
      #
      # @return [void]
      def rollback
        @internal_session.rollback
      end

      # Commits the session.
      #
      # This only has affect for :transacted sessions.
      #
      # @return [void]
      def commit
        @internal_session.commit
      end

      # Acknowledge any un-acknowledged messages in this session.
      #
      # This only has affect for :client_ack sessions.
      #
      # @return [void]
      def acknowledge
        @internal_session.acknowledge
      end

      # Closes the session.
      #
      # This will roll back the session if :transacted.
      #
      # @return [void]
      def close
        @internal_session.close
      end

      # @return [Symbol] The mode of this session.
      def mode
        case @internal_session.mode
        when WBSession::Mode::AUTO_ACK
          :auto_ack
        when WBSession::Mode::CLIENT_ACK
          :client_ack
        when WBSession::Mode::TRANSACTED
          :transacted
        end
      end

      protected

      def coerce_mode(mode)
        case mode
        when nil, :auto_ack
          WBSession::Mode::AUTO_ACK
        when :client_ack
          WBSession::Mode::CLIENT_ACK
        when :transacted
          WBSession::Mode::TRANSACTED
        else
          fail ArgumentError.new("#{mode} is not a valid session mode.")
        end

      end
    end
  end
end
