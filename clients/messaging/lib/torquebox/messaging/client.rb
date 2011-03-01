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

begin
  require 'torquebox-messaging'
rescue LoadError=>e
  # ignore!
end

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

      def self.connect(options={}, &block)
        transacted = options.fetch(:transacted, true)
        ack_mode = options.fetch(:ack_mode, AUTO_ACK)
        naming_host = options[:naming_host]
        naming_port = options[:naming_port]
        connection_factory = nil
        TorqueBox::Naming.connect( naming_host, naming_port ) do |context|
          connection_factory = context['/ConnectionFactory']
          connection = connection_factory.createConnection
          session = connection.createSession( transacted, canonical_ack_mode( ack_mode ) )
          connection.start
          session.naming_context = context
          session.connection = connection
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
