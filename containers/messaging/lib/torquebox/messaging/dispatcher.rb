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
require 'torquebox/messaging/message_processor_host'
require 'torquebox/naming'

module TorqueBox
  module Messaging

    # A service that may be configured with mappings of message
    # consumers to destinations
    class Dispatcher

      def initialize(options = {}, &block)
        @options = options
        instance_eval &block if block_given?
      end

      # Option keys are :filter and :config, either as symbol or string
      def map consumer, destination, options = {}
        options = options.inject({}) {|h,(k,v)| h[k.to_s]=v; h} # jruby won't convert symbols to strings
        processor = org.torquebox.messaging.deployers::MessagingYamlParsingDeployer::Parser.subscribe(consumer.to_s, destination.to_s, options)
        processor.ruby_require_path = nil if consumer.is_a? Class
        processors << processor
      end

      def start
        container.start
        @deployment = container.deploy( object_id.to_s )
        unit = container.deployment_unit( @deployment.name )
        processors.each do |processor|
          org.torquebox.mc::AttachmentUtils.multipleAttach( unit, processor, processor.name )
        end
        container.process_deployments(true)
      end

      def stop
        container.undeploy( @deployment )
        container.stop
      end

      def processors
        @processors ||= []
      end

      private
      
      def container
        unless @container

          TorqueBox::Naming.configure do |config|
            config.host = @options[:naming_host] unless @options[:naming_host].nil?
            config.port = @options[:naming_port] unless @options[:naming_port].nil?
          end

          @container = TorqueBox::Container::Foundation.new
          @container.enable( MessageProcessorHost )
        end
        @container
      end

    end
  end
end
