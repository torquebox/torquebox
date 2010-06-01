require 'torquebox/naming'
require 'torquebox/messaging/ext/javax_jms_session'

module TorqueBox
  module Messaging

    class Client

      AUTO_ACK = javax.jms::Session::AUTO_ACKNOWLEDGE
      CLIENT_ACK = javax.jms::Session::CLIENT_ACKNOWLEDGE
      DUPS_OK_ACK = javax.jms::Session::DUPS_OK_ACKNOWLEDGE

 
      def self.canonical_ack_mode(ack_mode)
        case ( ack_mode )
          when Fixnum
            return ack_mode
          when :auto
            return AUTO_ACK
          when :client
            return CLIENT_ACK
          when :dups_ok
            return DUPS_OK_ACK
        end
      end

      def self.connect(transacted=true, ack_mode=AUTO_ACK, naming_host='localhost', naming_port='1099', &block)
        connection_factory = nil
        TorqueBox::Naming.connect( naming_host, naming_port ) do |context|
          connection_factory = context['/ConnectionFactory']
          connection = connection_factory.createConnection
          session = connection.createSession( transacted, canonical_ack_mode( ack_mode ) )
          return session if ( block.nil? )
          begin
            block.call( session )
          ensure 
            connection.close()
          end 
        end
      end
    end

  end
end
