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

begin
  require 'arjdbc'
  TORQUEBOX_XA_JDBC = true
rescue LoadError
  TORQUEBOX_XA_JDBC = false
end
require 'set'

module TorqueBox
  module Transactions

    # These are only mixed in when ActiveRecord is loaded. They make
    # ActiveRecord's transactions distributed.
    # @api private
    module ActiveRecordAdapters

      module Connection

        if TORQUEBOX_XA_JDBC
          def transaction(*)
            super
          rescue ActiveRecord::JDBCError => e
            unless self.is_a?(XAResource)
              self.extend(XAResource)
              retry
            else
              raise
            end
          end
        end

      end

      module XAResource

        [ :begin_db_transaction, :commit_db_transaction, :rollback_db_transaction,
          :increment_open_transactions, :decrement_open_transactions, 
        ].each do |method|
          define_method(method) do 
            super unless Manager.current.active?
          end
        end

        # Defer execution of these tx-related callbacks invoked by
        # DatabaseStatements.transaction() until after the XA tx is
        # either committed or rolled back. 
        def commit_transaction_records(*)
          super if Manager.current.should_commit?(self)
        end
        def rollback_transaction_records(*)
          super if Manager.current.should_rollback?(self)
        end

      end

      module Transaction
        
        def prepare
          super
          # TODO: not this, but we need AR's pooled connection to
          # refresh from jboss *after* the transaction is begun.
          ActiveRecord::Base.clear_active_connections!
        end

        def error( exception )
          super
        rescue ActiveRecord::Rollback
        end

        def commit
          raise ActiveRecord::Rollback if @rolled_back
          super
          @complete = true
          connections.each { |connection| connection.commit_transaction_records }
        end

        def rollback
          super
          @complete = true
          connections.each do |connection| 
            connection.rollback_transaction_records(@transactions.empty?) 
          end
        end

        def should_commit?(connection)
          return true if @complete || !active?
          connections << connection
          false
        end

        def should_rollback?(connection)
          return true if @complete || !active?
          connections << connection
          @rolled_back = true
          false
        end
        
        def connections
          @connections ||= Set.new
        end
      end
      
    end
  end
end

if TORQUEBOX_XA_JDBC
  module ActiveRecord
    # @api private
    module ConnectionAdapters
      class JdbcAdapter
        include TorqueBox::Transactions::ActiveRecordAdapters::Connection
      end
    end
  end
end

module TorqueBox
  module Transactions
    class Manager
      include TorqueBox::Transactions::ActiveRecordAdapters::Transaction
    end
  end
end

module ActiveRecord
  module ConnectionAdapters
    module JndiConnectionPoolCallbacks
      def self.prepare(adapter, conn)
        if ActiveRecord::Base.respond_to?(:connection_pool) && conn.jndi_connection?
          adapter.extend self
        end
      end
    end
  end
end

