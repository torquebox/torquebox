# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

  # TorqueBox.transaction(resources...)
  def self.transaction(*resources, &block)
    Transactions::Manager.current.run(*resources, &block)
  end
  
  # Abstractions for distributed transactions
  module Transactions

    # The default module mixed into the Manager. Adapters for various
    # resources are expected to override these methods as appropriate
    # for their library. See ActiveRecordAdapters::Transaction, for
    # example.  These are the methods invoked by Manager#with_tx()
    module Transaction

      def prepare
        @transactions.push( @tm.suspend ) if active?
        @tm.begin
      end

      def commit
        @tm.commit
      end

      def rollback
        @tm.rollback
      end

      def cleanup
        @tm.suspend
        if @transactions.last
          @tm.resume( @transactions.pop )
        else
          Thread.current[:torquebox_transaction] = nil
        end
      end

      def error( exception )
        puts "Transaction rollback: #{exception}"
        rollback
        raise
      end

    end

    # The thread-local Manager encapsulates the interaction with the
    # *real* JBoss TransactionManager
    class Manager
      include TorqueBox::Injectors
      include Transaction

      def self.current()
        Thread.current[:torquebox_transaction] ||= new
      end

      def initialize()
        @tm = fetch('transaction-manager')
        @transactions = []
      end

      # Associate a list of resources with the current transaction. A
      # resource is expected to either implement XAResource or
      # respond_to?(:xa_resource)
      def enlist(*resources)
        resources.each do |resource| 
          xa = resource.is_a?( javax.transaction.xa.XAResource ) ? resource : resource.xa_resource
          @tm.transaction.enlist_resource(xa)
        end
      end

      # Where we either begin a new transaction (with_tx) or simply
      # yield as part of the current transaction.
      def run(*args, &block)
        return yield unless @tm
        opts = args.last.is_a?(Hash) ? args.pop : {}
        if active? && !opts[:requires_new]
          enlist(*args)
          yield
        else
          with_tx do
            enlist(*args)
            block.call
          end
        end
      end

      # Is there an active transaction?
      def active?
        @tm.transaction != nil
      end

      # The heart of the matter
      def with_tx
        prepare
        result = yield
        commit
        result
      rescue Exception => e
        error(e)
      ensure
        cleanup
      end

    end
  end
end

if defined?(ActiveRecord)
  require 'torquebox/active_record_adapters'
end

