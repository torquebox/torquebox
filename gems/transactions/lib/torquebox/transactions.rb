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

require 'torquebox/injectors'

module TorqueBox

  module Transactions
    
    module ConnectionAdapters

      def transaction(*)
        begin
          super
        rescue ActiveRecord::JDBCError => e
          unless self.is_a?(XAResource)
            puts "Creating an XAResource; exception=#{e}"
            self.extend(XAResource)
            retry
          end
        end
      end

    end

    module XAResource

      # An XA connection is not allowed to begin/commit/rollback
      def begin_db_transaction
        puts "JC: tx begin"
      end
      def commit_db_transaction
        puts "JC: tx commit"
      end
      def rollback_db_transaction
        puts "JC: tx rollback"
      end

      # Defer execution of these tx-related callbacks invoked by
      # DatabaseStatements.transaction() until after the XA tx is
      # either committed or rolled back. 
      def commit_transaction_records(*)
        super if Transaction.current.should_commit?(self)
      end
      def rollback_transaction_records(*)
        super if Transaction.current.should_rollback?(self)
      end

    end

    class Transaction
      include TorqueBox::Injectors

      def self.current()
        Thread.current[:torquebox_transaction] ||= new
      end

      def initialize()
        @tm = inject('transaction-manager')
        @resources = []
        @connections = []
      end

      def enlist(*resources)
        if begun?
          (resources + @resources).each { |r| @tm.transaction.enlist_resource(r) }
          @resources = []
        else
          @resources += resources
        end
        self
      end

      def run &block
        if begun?
          yield
        else
          with_tx &block
        end
      end

      def begun?
        @tm.transaction != nil
      end

      def with_tx
        begin
          @tm.begin
          enlist
          # TODO: This is wasteful when not using a JBoss XA datasource
          ActiveRecord::Base.connection.reconnect! if defined?(ActiveRecord)
          yield
          commit
        rescue Exception => e
          puts "Transaction rollback: #{e}", $@
          rollback
          raise unless e.is_a?(ActiveRecord::Rollback)
        ensure
          @tm.suspend
          Thread.current[:torquebox_transaction] = nil
        end
      end

      def should_commit?(connection)
        return true if @complete
        @connections << connection
        false
      end

      def should_rollback?(connection)
        return true if @complete
        @connections << connection
        @rolled_back = true
        false
      end

      def commit
        raise ActiveRecord::Rollback if @rolled_back
        @tm.commit
        @complete = true
        @connections.each { |connection| connection.commit_transaction_records }
      end

      def rollback
        @tm.rollback
        @complete = true
        @connections.each { |connection| connection.rollback_transaction_records(:all) }
      end
    end

  end
  
  # TorqueBox.transaction(resources...)
  def self.transaction(*resources, &block)
    if defined?(ActiveRecord)
      unless ActiveRecord::ConnectionAdapters::JdbcAdapter.include? TorqueBox::Transactions::ConnectionAdapters
        ActiveRecord::ConnectionAdapters::JdbcAdapter.send(:include, TorqueBox::Transactions::ConnectionAdapters)
      end
    end
    Transactions::Transaction.current.enlist(*resources).run &block
  end

end
