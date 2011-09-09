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
require 'torquebox/transactions'

module TorqueBox
  module Messaging
    class ProcessorWrapper
      
      include TorqueBox::Injectors

      def initialize(target, session, message)
        @target = target
        @session = session
        @message = message
        @tm = inject('transaction-manager')
      end

      def process!
        begin
          @tm.begin
          @tm.transaction.enlist_resource( @session.xa_resource )
          Thread.current[:session] = @session
          @target.process!( @message )
          if TorqueBox::Transactions.rolled_back?
            @tm.rollback
          else
            @tm.commit
          end
          TorqueBox::Transactions.run_callbacks!
        rescue Exception => e
          puts "Transaction exception: #{e}", $@
          @tm.rollback
          TorqueBox::Transactions.run_callbacks! :force_rollback
        ensure
          @tm.suspend
          Thread.current[:session] = nil
        end
      end
      
    end
  end
end
