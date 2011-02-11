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

require 'org.torquebox.container-foundation'
require 'torquebox/container/foundation'
require 'tmpdir'

module TorqueBox
  module Messaging
    class MessageBroker
      
      attr_accessor :data_dir

      def initialize(&block)
        instance_eval( &block ) if block
      end

      def fundamental_deployment_paths()
        [ File.join( File.dirname(__FILE__), 'message-broker-jboss-beans.xml' ) ]
      end

      def before_start(container)
        org.hornetq.core.logging::Logger.setDelegateFactory( org.hornetq.integration.logging::Log4jLogDelegateFactory.new )
        Java::java.lang::System.setProperty( "jboss.server.data.dir", data_dir || Dir.tmpdir )

	config_path = File.expand_path( File.join( File.dirname(__FILE__), 'hornetq-configuration.xml' )  )
	if ( config_path[0,1] != '/' )
	  config_path = "/#{config_path}"
	end
	config_url = "file://#{config_path}"
        java.lang::System.setProperty( "torquebox.hornetq.configuration.url", 
                                       config_url )
      end
    

    end
  end
end
