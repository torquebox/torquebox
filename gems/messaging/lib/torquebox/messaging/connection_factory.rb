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

require 'java'
require 'torquebox/messaging/connection'

module TorqueBox
  module Messaging
    class ConnectionFactory

      attr_reader :internal_connection_factory

      def self.new(internal_connection_factory = nil)
        return internal_connection_factory if internal_connection_factory.is_a?( ConnectionFactory )
        super
      end

      def initialize(internal_connection_factory = nil)
        @internal_connection_factory = internal_connection_factory
        @tm = TorqueBox.fetch('transaction-manager')
      end

      def with_new_connection(options, enlist_tx = true, &block)
        client_id = options[:client_id]
        create_internal_connection_factory( options )
        if !enlist_tx || (current.nil? && !transaction)
          connection = create_connection( options )
          connection.client_id = client_id if client_id
          begin
            connection.start
            result = block.call( connection )
          ensure
            connection.close
          end
        elsif transaction && (!current.respond_to?(:session_transaction) || current.session_transaction != transaction)
          result = block.call( activate( create_xa_connection( options ), client_id ) )
          # XaSession's afterCompletion callback deactivates XA connections
        else
          result = block.call( current )
        end
        result
      end

      def connections
        Thread.current[:torquebox_connection] ||= []
      end

      def current
        connections.last
      end

      def activate(connection, client_id)
        connection.client_id = client_id if client_id
        connection.start
        connections.push(connection) && current
      end

      def deactivate
        connections.pop.close
      end

      def transaction
        @tm && @tm.transaction
      end

      def create_internal_connection_factory(options)
        host     = options[:host] || "localhost"
        port     = options[:port] || 5445
        if !@internal_connection_factory
          @internal_connection_factory = create_connection_factory( host, port )
        end
      end

      def create_connection(options={})
        username = options[:username]
        password = options[:password]
        Connection.new( @internal_connection_factory.create_connection( username, password ), self )
      end

      def create_xa_connection(options={})
        username = options[:username]
        password = options[:password]
        XaConnection.new( @internal_connection_factory.create_xa_connection( username, password ), self )
      end

      def create_connection_factory(host, port)
        connect_opts = { "host" => host, "port" => port }
        transport_config =
          org.hornetq.api.core.TransportConfiguration.new("org.hornetq.core.remoting.impl.netty.NettyConnectorFactory",
                                                          connect_opts)
        org.hornetq.api.jms.HornetQJMSClient.createConnectionFactoryWithoutHA( org.hornetq.api.jms::JMSFactoryType::CF, transport_config )
      end


      def to_s
        "[ConnectionFactory: internal_connection_factory=#{internal_connection_factory}]"
      end

    end
  end
end
