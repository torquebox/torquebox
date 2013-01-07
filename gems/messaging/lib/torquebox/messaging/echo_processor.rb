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

module Torquebox
  module Messaging
    # A message processor that echos any messages sent to it back to
    # another queue specified by the response_queue configuration option
    class EchoProcessor < TorqueBox::Messaging::MessageProcessor

      def initialize(options={})
        @response_queue = TorqueBox::Messaging::Queue.new(options['response_queue'])
      end

      def on_message(body)
        @response_queue.publish(body)
      end

    end
  end
end
