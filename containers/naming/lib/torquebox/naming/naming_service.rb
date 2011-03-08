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

#require 'torquebox-container-foundation'
require 'torquebox/container/foundation'

module TorqueBox
  module Naming
    class NamingService

      def fundamental_deployment_paths
        paths = [ File.join( File.dirname(__FILE__), 'naming-local-jboss-beans.xml' ) ]
        if ( @export )
          paths << File.join( File.dirname(__FILE__), 'naming-rmi-jboss-beans.xml' )
        end
        paths
      end

      attr_accessor :port
      attr_accessor :host

      attr_accessor :rmi_port
      attr_accessor :rmi_host

      attr_accessor :export

      def initialize(&block)
        @host = 'localhost'
        @port = 1099

        @rmi_host = 'localhost'
        @rmi_port = 1098

        @export = true

        instance_eval(&block) if block
      end

      def port
        @port == 0 ? (@port = available_port) : @port
      end

      def rmi_port
        @rmi_port == 0 ? (@rmi_port = available_port) : @rmi_port
      end

      def before_start(container)
        Java::java.lang::System.setProperty( 'java.naming.factory.initial',  'org.jnp.interfaces.NamingContextFactory' )
        Java::java.lang::System.setProperty( 'java.naming.factory.url.pkgs', 'org.jboss.naming:org.jnp.interfaces' )
        Java::java.lang::System.setProperty( 'jnp.host', self.host.to_s )
        Java::java.lang::System.setProperty( 'jnp.port', self.port.to_s )
        Java::java.lang::System.setProperty( 'jnp.rmiHost', self.rmi_host.to_s )
        Java::java.lang::System.setProperty( 'jnp.rmiPort', self.rmi_port.to_s )
      end

      #def after_start(container)
      #  naming = container['JNDIServer'].naming_instance
      #  puts "naming is #{naming.inspect}"
      #  org.jnp.interfaces::NamingContext.setLocal( naming.to_java )
      #end

      def available_port
        server = TCPServer.new('127.0.0.1', 0)
        port = server.addr[1]
        server.close
        port
      end

    end
  end
end
