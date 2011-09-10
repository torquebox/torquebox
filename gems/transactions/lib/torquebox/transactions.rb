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

  module Transactions

    def transaction(*)
      begin
        super
      rescue ActiveRecord::JDBCError => e
        unless self.is_a?(XAResource)
          puts "JC: extend XAResource due to #{e}"
          self.extend(XAResource)
          retry
        end
      end
    end

    def self.rolled_back?
      conns = Thread.current[:tb_tx_records]
      conns && conns.any? {|type,conn| type == :rollback}
    end

    def self.run_callbacks!( force_rollback = false )
      begin
        Thread.current[:tb_running_callbacks] = true
        if force_rollback || rolled_back?
          (Thread.current[:tb_tx_records] || []).each {|type,conn| conn.rollback_transaction_records(:all)}
        else
          (Thread.current[:tb_tx_records] || []).each {|type,conn| conn.commit_transaction_records}
        end
      ensure
        Thread.current[:tb_tx_records] = nil
        Thread.current[:tb_running_callbacks] = nil
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
    # either committed or rolled back. Each invocation pushes a
    # 2-element array onto a thread-local array, the first element
    # indicating either commit or rollback. This is necessary since
    # not all rollbacks will raise an exception,
    # i.e. ActiveRecord::Rollback is not re-raised.
    def commit_transaction_records(*)
      if Thread.current[:tb_running_callbacks] 
        super 
      else
        (Thread.current[:tb_tx_records] ||= []) << [:commit, self]
      end
    end
    def rollback_transaction_records(*)
      if Thread.current[:tb_running_callbacks] 
        super 
      else
        (Thread.current[:tb_tx_records] ||= []) << [:rollback, self]
      end
    end

  end

  # TorqueBox.transaction(resources...)
  def self.transaction(*resources)
    if defined?(ActiveRecord)
      unless ActiveRecord::ConnectionAdapters::JdbcAdapter.include? TorqueBox::Transactions
        ActiveRecord::ConnectionAdapters::JdbcAdapter.send(:include, TorqueBox::Transactions)
      end
      ActiveRecord::Base.connection.reconnect!
    end
    yield
  end

end
