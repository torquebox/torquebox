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

module TorqueBox
  module Messaging

    attr_accessor :jms_connection
    
    class Connection
      def initialize(jms_connection, hornetq_direct)
        @jms_connection = jms_connection
        @hornetq_direct = hornetq_direct
      end

      def start
        @jms_connection.start
      end

      def close
        @jms_connection.close
      end

      def client_id
        @jms_connection.client_id
      end

      def client_id=(client_id)
        @jms_connection.client_id = client_id
      end
      
      def with_new_session(transacted=true, ack_mode=Session::AUTO_ACK, &block)
        session = self.create_session( transacted, ack_mode )
        begin
          result = block.call( session )
        ensure
          session.commit if session.transacted?
          session.close
        end
        return result
      end

      def create_session(transacted=true, ack_mode=Session::AUTO_ACK)
        session = @jms_connection.create_session( transacted, Session.canonical_ack_mode( ack_mode ) )
        @hornetq_direct ? HornetQSession.new( session, self ) : Session.new( session, self )
      end

    end
  end
end
