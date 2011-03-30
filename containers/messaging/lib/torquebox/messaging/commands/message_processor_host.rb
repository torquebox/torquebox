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
require 'torquebox/messaging/message_processor_host'
require 'torquebox/messaging/metadata_builder'
require 'torquebox/naming'

module TorqueBox
  module Messaging
    module Commands

      class MessageProcessorHost < TorqueBox::Container::FoundationCommand

        attr_accessor :deploy_files
        def initialize()
          super
          @deploy_files = []
          @deployments = []
          @naming_service = nil
        end

        def configure(container)
          TorqueBox::Naming.configure do |config|
            config.host = @naming_service 
          end
          container.enable( TorqueBox::Messaging::MessageProcessorHost )
        end

        def after_start(container)
          @deploy_files.each do |file|
            deployment = container.deploy( file )
            unit = container.deployment_unit( deployment.name )
            builder = TorqueBox::Messaging::MetaData::Builder.new() 
            builder.evaluate_file( file )
            builder.processors.each do |processor|
              org.torquebox.mc::AttachmentUtils.multipleAttach( unit, processor, processor.name )
            end
            app_meta = org.torquebox.base.metadata::RubyApplicationMetaData.new
            app_meta.setApplicationName( "none" )
            app_meta.setEnvironmentName( ENV['TORQUEBOX_ENV'] || 'development' )
            app_meta.setRoot( org.jboss.vfs::VFS.getChild( Dir.pwd ) )
            unit.addAttachment( org.torquebox.base.metadata::RubyApplicationMetaData.java_class, app_meta )
            container.process_deployments(true)
            puts "deployed #{file}"
            @deployments << deployment
          end
        end

        def before_stop(container)
          @deployments.reverse.each do |deployment|
            container.undeploy( deployment )
          end
        end

        def parser_options(opts)
          opts.on( '-d', '--deploy FILE', 'Deploy a message-processor configuration' ) do |file|
            @deploy_files << file
          end
          opts.on( '-n', '--naming HOST', 'External naming service connection' ) do |naming_service|
            @naming_service = naming_service
          end
          super( opts )
        end

      end

    end
  end
end
