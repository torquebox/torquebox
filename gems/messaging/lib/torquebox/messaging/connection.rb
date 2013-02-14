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
require 'torquebox/messaging/session'

module TorqueBox
  module Messaging
    class Connection
      include TorqueBox::Injectors

      attr_accessor :jms_connection

      def initialize(jms_connection, connection_factory)
        @jms_connection = jms_connection
        @connection_factory = connection_factory
        @tm = fetch('transaction-manager')
      end

      def start
        jms_connection.start
      end

      def close
        jms_connection.close
      end

      def client_id
        jms_connection.client_id
      end

      def client_id=(client_id)
        jms_connection.client_id = client_id
      end

      def with_session(&block)
        begin
          session = create_session
          result = block.call( session )
        ensure
          session.close
        end
        result
      end

      def create_session
        Session.new( jms_connection.create_session( false, Session::AUTO_ACK ) )
      end

      def transaction
        @tm && @tm.transaction
      end

      def deactivate
        @connection_factory.deactivate
      end

    end

  end
end
