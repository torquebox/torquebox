# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

require 'torquebox/messaging/destination'
require 'torquebox/service_registry'

module TorqueBox
  module Messaging
    class Queue < Destination

      def self.start( name, options={} )
        selector = options.fetch( :selector, "" )
        durable  = options.fetch( :durable,  true )
        jndi     = options.fetch( :jndi,     [].to_java(:string) )
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.default.jms.manager") do |server|
          server.createQueue( false, name, selector, durable, jndi )
        end
        new( name )
      end

      def stop
        TorqueBox::ServiceRegistry.lookup("jboss.messaging.default.jms.manager") do |server|
          server.destroyQueue( name )
        end
      end

      def publish_and_receive(message, options={})
        result = nil
        with_session do |session|
          result = session.publish_and_receive(self, message,
                                               normalize_options(options))
        end
        result
      end

      def receive_and_publish(options={}, &block)
        with_session do |session|
          session.receive_and_publish(self, normalize_options(options), &block)
        end
      end

      def to_s
        "[Queue: #{super}]"
      end
    end
  end
end
