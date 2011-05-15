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

require 'torquebox/messaging/destination'

module TorqueBox
  module Messaging

    class Task

      def self.queue_name
        suffix = org.torquebox.common.util.StringUtils.underscore(name[0...-4])
        "/queues/torquebox/#{ENV['TORQUEBOX_APP_NAME']}/tasks/#{suffix}"
      end

      def self.async(method, payload = {}, options = {})
        message = {:method => method, :payload => payload}
        Queue.new(queue_name).publish message, options
      rescue javax.naming.NameNotFoundException => ex
        raise RuntimeError.new("The queue for #{self.name} is not available. Did you disable it by setting its concurrency to 0?")
      end

      def process!(message)
        hash = message.decode
        self.send hash[:method].to_sym, hash[:payload]
      end

    end

  end
end

