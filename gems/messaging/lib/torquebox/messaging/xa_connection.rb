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

module TorqueBox
  module Messaging
    class XaConnection < Connection

      def with_session(&block)
        # Re-use a single XaSession per XaConnection
        # This session gets closed by the afterCompletion
        # callback on XaSession
        @session ||= create_session
        block.call( @session )
      end

      def create_session(auto_enlist = true)
        jms_session = jms_connection.create_xa_session
        session = XaSession.new( jms_session, transaction, self )
        if auto_enlist
          transaction.enlist_resource( jms_session.xa_resource )
          transaction.registerSynchronization( session )
        end
        session
      end

      def session_transaction
        @session.nil? ? nil : @session.transaction
      end

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
