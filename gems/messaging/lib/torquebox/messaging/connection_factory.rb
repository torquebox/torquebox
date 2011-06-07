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

      def initialize(internal_connection_factory = null)
        @internal_connection_factory = internal_connection_factory
        @hornetq_direct = false
      end

      def with_new_connection(&block)
        connection = create_connection
        connection.start
        begin
          result = block.call( connection )
        ensure
          connection.close
        end
        return result
      end

      def create_connection()
        if !@internal_connection_factory
          # try to connect to HornetQ directly - this currently
          # assumes localhost, and the default AS7 HQ Netty port of 5445
          connect_opts = { org.hornetq.core.remoting.impl.netty.TransportConstants::PORT_PROP_NAME => 5445.to_java( java.lang.Integer ) }
          transport_config =
            org.hornetq.api.core.TransportConfiguration.new("org.hornetq.core.remoting.impl.netty.NettyConnectorFactory", 
                                                            connect_opts)
          @internal_connection_factory =
            org.hornetq.api.jms.HornetQJMSClient.createConnectionFactoryWithoutHA( org.hornetq.api.jms::JMSFactoryType::CF, transport_config )
          @hornetq_direct = true
        end

        Connection.new( @internal_connection_factory.create_connection, @hornetq_direct )
      end


      def to_s
        "[ConnectionFactory: internal_connection_factory=#{internal_connection_factory}]"
      end

    end
  end
end
