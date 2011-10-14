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

require 'json/pure'
require 'json/add/core'

module TorqueBox
  module Messaging
    class JSONMessage < Message
      ENCODING = :json
      JMS_TYPE = :text
      
      def encode(message)
        @jms_message.text = JSON.fast_generate( message ) unless message.nil?
      end

      def decode
        # we can't :symbolize_names here, since that breaks turning
        # json_class back into an object, ffs
        JSON.parse( @jms_message.text ) unless @jms_message.text.nil?
      end

    end

    Message.register_encoding( JSONMessage )
  end
end
