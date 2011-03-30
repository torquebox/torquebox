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

require 'optparse'

require 'torquebox/container/foundation'
require 'torquebox/container/foundation_command'
require 'torquebox/messaging/message_broker'
require 'torquebox/naming'
require 'torquebox/naming/naming_service'

module TorqueBox
  module Messaging
    module Commands

      class MessageBroker < TorqueBox::Container::FoundationCommand

        attr_accessor :deploy_files
        def initialize()
          super
          @deploy_files = []
          @deployments = []
          @naming_service = nil
          @standalone = false
          @bind_address = nil
        end

        def configure(container)
          if ( @standalone )
            container.enable( TorqueBox::Naming::NamingService )
          else
            TorqueBox::Naming.configure do |config|
              config.host = @naming_service
            end
          end
          container.enable( TorqueBox::Messaging::MessageBroker )
        end

        def after_start(container)
          require 'vfs'
          @deploy_files.each do |file|
            deployment = container.deploy( file )
            unit = container.deployment_unit( deployment.name )
            virtual_file = org.jboss.vfs::VFS.child( File.join( Dir.pwd, file ) )
            unit.addAttachment( 'queues.yml.altDD', virtual_file )
            container.process_deployments(true)
            @deployments << deployment
          end
        end

        def before_stop(container)
          @deployments.reverse.each do |deployment|
            container.undeploy( deployment.name )
          end
        end

        def parser_options(opts)
          opts.on( '-d', '--deploy FILE', 'Deploy a destination descriptor' ) do |file|
            @deploy_files << file
          end
          opts.on( '-n', '--naming HOST', 'External naming service connection' ) do |naming_service|
            @naming_service = naming_service
          end
          opts.on( '-s', '--standalone', 'Run standalone (include naming)' ) do |host|
            @standalone = true
          end
          opts.on( '-b', '--bind', 'Bind address' ) do |bind_address|
            @bind_address = bind_address
          end
          super( opts )
        end

      end

    end
  end
end
