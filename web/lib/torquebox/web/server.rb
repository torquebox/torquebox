# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'torquebox/option_utils'
require 'torquebox/spec_helpers'

module TorqueBox

  # Example usage of an embedded TorqueBox web server, including SockJS:
  # {include:file:integration-tests/apps/embedded/sockjs_echo/main.rb}
  module Web

    # @!macro server_options
    #   @option options [String] :host ('localhost') the host to bind the server
    #     to
    #   @option options [Fixnum] :port (8080) the port to bind the server to
    #   @option options [Boolean] :auto_start (true) whether to start the web
    #     server as soon as an application is deployed or wait until
    #     {Server#start} is called.


    DEFAULT_SERVER_OPTIONS = {
      :host => 'localhost',
      :port => 8080,
      :auto_start => true
    }

    # @!macro mount_options
    #   @option options [String] :root ('.') the application's root directory
    #   @option options [String] :path ('/') the context path to mount this
    #     application under
    #   @option options [String] :static_dir ('public') the directory containing
    #     static assets that should be served by TorqueBox. A value of nil
    #     disables static asset serving.
    #   @option options [String] :rackup ('config.ru') the rackup file to use
    #     when running the application. This is ignored if :rack_app is given.
    #   @option options [Object] :rack_app the Rack application to run. This
    #     is useful if the application is created somewhere else before handing
    #     the Rack application object off to TorqueBox to run.


    DEFAULT_MOUNT_OPTIONS = {
      :root => '.',
      :path => '/',
      :static_dir => 'public',
      :rackup => 'config.ru',
      :rack_app => nil
    }

    # Runs a Rack application specified by the given options.
    #
    # This is a shortcut for the most common pattern of calling
    # {server} followed by {Server#mount}.
    #
    # @!macro server_options
    # @!macro mount_options
    # @option options [String] :server ('default') the web server to use when
    #   running this application. This is only needed if you want to start
    #   more than one server listening on different hosts or ports. When
    #   running inside a Servlet container, host and port options for this
    #   'default' server will be ignored and it will use the container's
    #   web server.
    #
    # @return [Server]
    #
    # @example Run the Rack application in the current directory
    #   TorqueBox::Web.run
    # @example Specify a custom rackup file and non-root context path
    #   TorqueBox::Web.run(:rackup => 'other_config.ru', :path => '/other')
    # @example Run applications on multiple web servers bound to different hosts and ports
    #   TorqueBox::Web.run(:host => '0.0.0.0', :port => 8080)
    #   TorqueBox::Web.run(:server => 'another', :host => '127.0.0.1',
    #                      :port => 8081, :root => 'admin_console/')
    def self.run(options = {})
      server_name = options.delete(:server) { 'default' }
      server_options = options.reject { |k, v| DEFAULT_MOUNT_OPTIONS.key?(k) }
      server = server(server_name, server_options)
      mount_options = options.reject { |k, v| DEFAULT_SERVER_OPTIONS.key?(k) }
      server.mount(mount_options)
      server
    end

    # Return a {Server} created from the given options
    #
    # @!macro server_options
    #
    # @return [Server]
    def self.server(name, options = {})
      Server.new(name, options)
    end

    class Server
      include TorqueBox::OptionUtils
      java_import org.projectodd.wunderboss.rack.RackHandler
      java_import org.projectodd.sockjs.SockJsServer
      java_import org.projectodd.sockjs.servlet.SockJsServlet

      # @api private
      WB = org.projectodd.wunderboss.WunderBoss

      # @api private
      WBWeb = org.projectodd.wunderboss.web.Web

      # @api private
      attr_accessor :web_component

      # Mount a Rack application under a specific context path on this server.
      #
      # @!macro mount_options
      def mount(options = {})
        options = DEFAULT_MOUNT_OPTIONS.merge(options)
        valid_keys = opts_to_set(WBWeb::RegisterOption) + DEFAULT_MOUNT_OPTIONS.keys
        validate_options(options, valid_keys)
        @logger.debug("Mounting context path {} with options {} on TorqueBox::Web::Server '{}'",
                      options[:path], options.inspect, @web_component.name)
        servlet_context = WB.options.get("servlet-context-path", "")
        relative_root = servlet_context + options[:path]
        relative_root.chop! if relative_root.end_with?("/")
        ENV["RAILS_RELATIVE_URL_ROOT"] = relative_root
        if options[:rack_app].nil?
          require 'rack'
          rackup = File.expand_path(options[:rackup], options[:root])
          options[:rack_app] = Rack::Builder.parse_file(rackup).first
        end
        if options[:init]
          options[:init] = options[:init].to_java(java.lang.Runnable)
        end
        handler = RackHandler.new(options[:rack_app], options[:path])
        register_options = extract_options(options, WBWeb::RegisterOption)
        @logger.trace("Registering handler at context path {}", options[:path])
        @web_component.register_handler(handler, register_options)
        handler
      end

      # @!macro mount_servlet_options
      #   @option options [String] :path ('/') the context path to mount this
      #     servlet under


      DEFAULT_MOUNT_SERVLET_OPTIONS = {
        :path => '/'
      }

      # Mount a Servlet under a specific context path on this server
      #
      # @param servlet [javax.servlet.Servlet] the servlet to mount
      #
      # @!macro mount_servlet_options
      def mount_servlet(servlet, options = {})
        options = DEFAULT_MOUNT_SERVLET_OPTIONS.merge(options)
        valid_keys = opts_to_set(WBWeb::RegisterOption) + DEFAULT_MOUNT_SERVLET_OPTIONS.keys
        validate_options(options, valid_keys)
        @logger.debug("Mounting servlet {} with options {} on TorqueBox::Web::Server '{}'",
                      servlet, options.inspect, @web_component.name)
        register_options = extract_options(options, WBWeb::RegisterOption)
        @web_component.register_servlet(servlet, register_options)
      end

      # Unmount the Rack application or servlet at the given context path.
      #
      # @param path [String] the context path to unmount
      def unmount(path)
        @web_component.unregister(path)
      end

      # Mount a SockJS endpoint under a specific context path on this server
      #
      # @!macro mount_servlet_options
      def sockjs(options = {})
        sockjs_server = SockJsServer.new
        # TODO: handle options
        servlet = SockJsServlet.new(sockjs_server)
        mount_servlet(servlet, options)
        sockjs_server
      end

      # Start the server
      def start
        @logger.info("Starting TorqueBox::Web::Server '{}'",
                     @web_component.name)
        @web_component.start
      end

      # Stop the server
      def stop
        @logger.info("Stopping TorqueBox::Web::Server '{}'",
                     @web_component.name)
        @web_component.stop
      end

      # @api private
      def run_from_cli
        # Handle starting the server and listening for signals to shutdown
        start
        TorqueBox::SpecHelpers.booted
        thread = Thread.current
        Signal.trap("INT") do
          org.projectodd.wunderboss.WunderBoss.shutdown_and_reset
          thread.wakeup
        end
        Signal.trap("TERM") do
          org.projectodd.wunderboss.WunderBoss.shutdown_and_reset
          thread.wakeup
        end
        sleep
      end


      protected

      def initialize(name, options = {})
        @logger = WB.logger('TorqueBox::Web::Server')
        options = DEFAULT_SERVER_OPTIONS.merge(options)
        validate_options(options, DEFAULT_SERVER_OPTIONS.keys)
        create_options = extract_options(options, WBWeb::CreateOption)
        web = WB.find_or_create_component(WBWeb.java_class, name,
                                          create_options)
        @logger.debug("TorqueBox::Web::Server '{}' has component {}",
                      name, web)
        @web_component = web
      end

    end
    
    module Undertow

      # Exposes tuning options for an Undertow web server by returning
      # an options map that includes an Undertow::Builder instance
      # mapped to :configuration.
      # 
      # It takes the same options as {TorqueBox::Web#run} plus the
      # following: 
      #
      # @option options [Fixnum] :io_threads the number of IO threads
      #   defaults to available processors
      # @option options [Fixnum] :worker_threads the number of worker
      #   threads defaults to 8 * io_threads
      # @option options [Fixnum] :buffer_size in bytes
      # @option options [Fixnum] :buffers_per_region defaults to
      #   either 10 or 20 if > 128Mb of RAM
      # @option options [Boolean] :direct_buffers? defaults to true if
      #   > 128Mb of RAM
      #
      # The return value is an options map with the :configuration
      # option replacing the ones relevant to an Undertow::Builder
      def self.builder(options = {})
        builder = Java::io.undertow.Undertow.builder
        host = options[:host] || "localhost"
        port = options[:port] || 8080
        builder.addHttpListener(port, host)
        builder.setIoThreads(options[:io_threads]) if options[:io_threads]
        builder.setWorkerThreads(options[:worker_threads]) if options[:worker_threads]
        builder.setBufferSize(options[:buffer_size]) if options[:buffer_size]
        builder.setBuffersPerRegion(options[:buffers_per_region]) if options[:buffers_per_region]
        builder.setDirectBuffers(options[:direct_buffers?]) unless options[:direct_buffers?].nil?
        result = options.reject { |k,v| [:io_threads, :worker_threads, :buffer_size, :buffers_per_region, :direct_buffers?].include?(k) }
        result[:configuration] = builder
        result
      end

    end
  end
end
