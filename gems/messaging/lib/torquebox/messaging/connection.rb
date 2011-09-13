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
      
      def with_session(force_new = false, &block)
        force_new ? with_new_session(&block) : with_thread_local_session(&block)
      end

      def with_new_session
        session = self.create_session()
        begin
          result = yield( session )
        ensure
          session.close
        end
        return result
      end

      def with_thread_local_session(&block)
        current = Thread.current[:session]
        if current.nil?
          with_new_session do |session|
            Thread.current[:session] = session
            begin
              block.call( session )
            ensure
              Thread.current[:session] = nil
            end
          end
        else
          yield( current )
        end
      end

      def create_session()
        session = @jms_connection.create_xa_session()
        @hornetq_direct ? HornetQSession.new( session ) : Session.new( session )
      end

    end
  end
end
