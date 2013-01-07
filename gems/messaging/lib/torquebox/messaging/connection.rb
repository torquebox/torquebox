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

      def initialize(jms_connection)
        @jms_connection = jms_connection
        @tm = fetch('transaction-manager')
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
      
      def with_session(enlist_tx = true)
        if !enlist_tx || (current.nil? && !transaction)
          begin
            yield activate( create_session )
          ensure
            deactivate
          end
        elsif transaction && (!current.respond_to?(:transaction) || current.transaction != transaction)
          yield activate( create_xa_session )
        else
          yield( current )
        end
      end

      def sessions
        Thread.current[:session] ||= []
      end
      def current
        sessions.last
      end
      def activate(session)
        sessions.push(session) && current
      end
      def deactivate
        sessions.pop.close
      end

      def transaction
        @tm && @tm.transaction
      end

      def create_xa_session
        jms_session = @jms_connection.create_xa_session()
        transaction.enlist_resource( jms_session.xa_resource )
        session = TransactedSession.new( jms_session, transaction, self )
        transaction.registerSynchronization( session )
        session
      end

      def create_session
        Session.new( @jms_connection.create_session( false, Session::AUTO_ACK ) )
      end

    end
    
    class TransactedSession < Session
      include javax.transaction.Synchronization
      attr_reader :transaction

      def initialize( jms_session, transaction, connection )
        super( jms_session )
        @transaction = transaction
        @connection = connection.extend(TransactedConnection)
      end

      def close
        # eat the close, until tx completes
      end

      def beforeCompletion
        # required interface
      end

      def afterCompletion(status)
        @connection.deactivate
        @connection.complete!
      end
    end

    module TransactedConnection
      def close
        super if @complete
      end
      def complete!
        @complete = true
        close
      end
    end

  end
end
