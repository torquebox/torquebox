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

module TorqueBox
  module Messaging
    class MarshalMessage < Message
      ENCODING = :marshal
      JMS_TYPE = :bytes
      
      def encode(message)
        unless message.nil?
          marshalled = Marshal.dump( message )
          @jms_message.write_bytes( marshalled.to_java_bytes )
        end
      end

      def decode
        if (length = @jms_message.get_body_length) > 0
          bytes = Java::byte[length].new
          @jms_message.read_bytes( bytes )
          @jms_message.reset
          Marshal.restore( String.from_java_bytes( bytes ) )
        end
      end

    end

    Message.register_encoding( MarshalMessage )
  end
end
