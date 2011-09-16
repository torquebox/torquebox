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

  # TorqueBox.transaction(resources...)
  def self.transaction(*resources, &block)
    Transactions::Manager.current.run(*resources, &block)
  end

  module Transactions

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
      end

    end

    class Manager
      include TorqueBox::Injectors
      include Transaction

      def self.current()
        Thread.current[:torquebox_transaction] ||= new
      end

      def initialize()
        @tm = inject('transaction-manager')
        @transactions = []
      end

      def enlist(*resources)
        resources.each do |resource| 
          xa = resource.is_a?( javax.transaction.xa.XAResource ) ? resource : resource.xa_resource
          @tm.transaction.enlist_resource(xa)
        end
      end

      def run(*args, &block)
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

      def active?
        @tm.transaction != nil
      end

      def with_tx
        begin
          prepare
          yield
          commit
        rescue Exception => e
          error(e)
        ensure
          cleanup
        end
      end

    end
  end
end

if defined?(ActiveRecord)
  require 'torquebox/active_record_adapters'
end
