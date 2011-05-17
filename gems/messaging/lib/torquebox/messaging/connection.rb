
module TorqueBox
  module Messaging

    attr_accessor :connection
    
    class Connection
      def initialize(jms_connection, remote)
        @jms_connection = jms_connection
        @remote = remote
      end

      def start
        @jms_connection.start
      end

      def close
        @jms_connection.close
      end

      def with_new_session(transacted=true, ack_mode=Session::AUTO_ACK, &block)
        session = self.create_session( transacted, ack_mode )
        begin
          result = block.call( session )
        ensure
          session.close
        end
        return result
      end

      def create_session(transacted=true, ack_mode=Session::AUTO_ACK)
        session = @jms_connection.create_session( transacted, Session.canonical_ack_mode( ack_mode ) )
        @remote ? HornetQSession.new( session ) : Session.new( session )
      end

    end
  end
end
