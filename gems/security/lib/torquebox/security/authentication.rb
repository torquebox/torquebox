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

module TorqueBox
  module Authentication
    def self.[](name)
      return nil unless torquebox_context
      puts "Looking up authentication context #{service_name( name )}"
      Authenticator.new( ::TorqueBox::ServiceRegistry.lookup( service_name( name ) ) )
    end

    def self.default
      self['default']
    end

    def self.service_name(domain)
      "#{service_prefix}.#{torquebox_context}.#{domain}"
    end

    def self.service_prefix
      "torquebox.authentication"
    end

    def self.torquebox_context
      puts "ERROR: TorqueBox application context not available" unless ENV['TORQUEBOX_APP_NAME']
      ENV['TORQUEBOX_APP_NAME']
    end
  end

  class Authenticator
    def initialize(auth_bean)
      @auth_bean = auth_bean
      puts "Initializing TorqueBox Authenticator with #{auth_bean.inspect}"
    end

    def authenticate(user, pass, &block)
      if @auth_bean.nil?
        puts "ERROR: No authentication delegate found. Authentication not enabled." 
        return false 
      end

      authenticated = @auth_bean.authenticate(user, pass)
      block.call if authenticated && block
      authenticated
    end
  end
end

