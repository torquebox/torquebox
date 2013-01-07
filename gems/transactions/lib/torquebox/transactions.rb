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

      # Begin a transaction, yield, commit or rollback on error.
      def start
        prepare
        result = yield
        commit
        result
      rescue Exception => e
        error(e)
      end

      # Suspend current transaction, yield, and resume
      def suspend
        @transactions.push( @tm.suspend )
        yield
      ensure
        @tm.resume( @transactions.pop )
      end

      # JEE Required
      def required &block
        if active?
          yield
        else
          start &block
        end
      end

      # JEE RequiresNew
      def requires_new &block
        if active?
          suspend do
            start &block
          end
        else
          start &block
        end
      end

      # JEE NotSupported
      def not_supported &block
        if active?
          suspend &block
        else
          yield
        end
      end

      # JEE Supports
      def supports
        yield
      end

      # JEE Mandatory
      def mandatory
        if active?
          yield
        else
          raise "No active transaction"
        end
      end

      # JEE Never
      def never
        if active?
          raise "Active transaction detected"
        else
          yield
        end
      end

      # Returns a 2-element tuple [resources, method] where method is
      # a symbol corresponding to one of the JEE tx attribute methods
      # above. All but the last argument should be XA resources to be
      # enlisted in the transaction. The last argument passed is
      # expected to be either a symbol referring to one of the JEE
      # methods or a Hash in which the method symbol is associated
      # with the :scope key. If omitted, defaults to :required.
      #
      # For backwards compatibility the hash may also contain a
      # :requires_new key which, if true, will result in the
      # :requires_new method symbol being returned.
      #
      # :none may be used as an alias for :not_supported
      #
      # Whew!
      def parse_args(*args)
        resources, method = case args.last
                            when Symbol
                              last = args.pop
                              [args, last]
                            when Hash
                              hash = args.pop
                              last = hash[:scope] || (hash[:requires_new] ? :requires_new : :required)
                              [args, last]
                            else
                              [args, :required]
                            end
        method = :not_supported if method == :none
        [resources, method]
      end
      
      def run(*args, &block)
        return yield unless @tm
        resources, method = parse_args(*args)
        fn = lambda do
          enlist(*resources)
          block.call
        end
        send(method, &fn)
      ensure
        Thread.current[:torquebox_transaction] = nil if @transactions.empty?
      end

    end
  end
end

if defined?(ActiveRecord)
  require 'torquebox/active_record_adapters'
end

