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
    class XaConnectionFactory

      attr_reader :internal_connection_factory
      
      def self.new(internal_connection_factory = nil)
        return internal_connection_factory if internal_connection_factory.is_a?( XaConnectionFactory )
        super
      end

      def initialize(internal_connection_factory = nil)
        @internal_connection_factory = internal_connection_factory
      end

      def with_new_connection(options, &block)
        client_id = options[:client_id]
        connection = create_connection
        connection.client_id = client_id
        connection.start
        begin
          result = block.call( connection )
        ensure
          connection.close
        end
        return result
      end

      def create_connection()
        XaConnection.new( @internal_connection_factory.create_xa_connection )
      end


      def to_s
        "[XaConnectionFactory: internal_connection_factory=#{internal_connection_factory}]"
      end

    end
  end
end
