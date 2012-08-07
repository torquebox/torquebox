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
    # example.
    module Transaction

      def prepare
        @tm.begin
      end

      def commit
        @tm.commit
      end

      def rollback
        @tm.rollback
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

      # Is there an active transaction?
      def active?
        @tm.transaction != nil
      end

      def beguine
        prepare
        result = yield
        commit
        result
      rescue Exception => e
        error(e)
      end

      def suspend
        @transactions.push( @tm.suspend )
        yield
      ensure
        @tm.resume( @transactions.pop )
      end

      def required &block
        if active?
          yield
        else
          beguine &block
        end
      end

      def requires_new &block
        if active?
          suspend do
            beguine &block
          end
        else
          beguine &block
        end
      end
      
      def run(*args, &block)
        return yield unless @tm
        opts = args.last.is_a?(Hash) ? args.pop : {}
        fn = lambda do
          enlist(*args)
          block.call
        end
        if opts[:requires_new]
          requires_new &fn
        else
          required &fn
        end
      ensure
        Thread.current[:torquebox_transaction] = nil if @transactions.empty?
      end

    end
  end
end

if defined?(ActiveRecord)
  require 'torquebox/active_record_adapters'
end

