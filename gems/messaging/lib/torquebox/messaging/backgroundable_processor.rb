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

require 'torquebox/messaging/message_processor'
require 'torquebox/messaging/const_missing'
require 'torquebox/messaging/future_responder'

module TorqueBox
  module Messaging
    class BackgroundableProcessor < MessageProcessor

      def self.log_newrelic_notice(klass)
        @newrelic_notice_logged ||=
          log.warn( "The New Relic agent is loaded, but an issue with the inheritance hierachy of " <<
                    klass.name << " prevents us from reporting on its Backgroundable calls." ) || true
      end

      def on_message(hash)
        FutureResponder.new( Queue.new( hash[:future_queue] ), hash[:future_id] ).respond do
          klass = hash[:receiver].class
          if klass.respond_to?( :__enable_backgroundable_newrelic_tracing )
            klass.__enable_backgroundable_newrelic_tracing( hash[:method] )
          elsif Backgroundable.newrelic_available? 
            self.class.log_newrelic_notice( klass )
          end
          
          hash[:receiver].send(hash[:method], *hash[:args])
        end
      end

      private
      def self.log
        @logger ||= TorqueBox::Logger.new( self )
      end
    end
  end
end
