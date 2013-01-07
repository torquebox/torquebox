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

require 'torquebox/messaging/processor_middleware/default_middleware'

module TorqueBox
  module Messaging
    class MessageProcessor
      include ProcessorMiddleware::DefaultMiddleware
      
      attr_accessor :message

      def initialize
        @message = nil 
      end
      
      def on_message(body)
        throw "Your subclass must implement on_message(body)"
      end

      def on_error(error)
        raise error
      end

      def process!(message)
        @message = message
        begin
          on_message( message.decode )
        rescue Exception => e
          on_error( e ) 
        end 
      end

    end
  end
end
