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

    class XaSession < Session
      include javax.transaction.Synchronization
      attr_reader :transaction

      def initialize( jms_session, transaction, connection )
        super( jms_session )
        @transaction = transaction
        @connection = connection
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

      def xa_resource
        @jms_session.xa_resource
      end

    end

  end
end
