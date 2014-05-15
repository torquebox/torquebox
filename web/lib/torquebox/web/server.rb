require 'torquebox/option_utils'

module TorqueBox
  module Web
    class Server
      include TorqueBox::OptionUtils

      WB = org.projectodd.wunderboss.WunderBoss
      WBWeb = org.projectodd.wunderboss.web.Web
      java_import org.projectodd.wunderboss.rack.RackHandler
      java_import org.projectodd.sockjs.SockJsServer
      java_import org.projectodd.sockjs.servlet.SockJsServlet

      attr_accessor :web_component

      DEFAULT_CREATE_OPTIONS = {
        :host => 'localhost',
        :port => 8080,
        :auto_start => true
      }
      def self.find_or_create(name, options={})
        new(name, options)
      end

      def self.run(name, options={})
        create_options = options.reject { |k, v| DEFAULT_MOUNT_OPTIONS.has_key?(k) }
        server = new(name, create_options)
        mount_options = options.reject { |k, v| DEFAULT_CREATE_OPTIONS.has_key?(k) }
        server.mount(mount_options)
        server
      end

      def self.sockjs(context, options={})
        find_or_create('default').sockjs(context, options)
      end

      DEFAULT_MOUNT_OPTIONS = {
        :root => '.',
        :path => '/',
        :static_dir => 'public',
        :rackup => 'config.ru',
        :rack_app => nil
      }

      def mount(options={})
        options = DEFAULT_MOUNT_OPTIONS.merge(options)
        validate_options(options, opts_to_set(WBWeb::RegisterOption) + DEFAULT_MOUNT_OPTIONS.keys)
        @logger.debugf("Mounting context path %s with options %s on TorqueBox::Web::Server '%s'",
                       options[:path], options.inspect, @web_component.name)
        if options[:rack_app].nil?
          require 'rack'
          rackup = File.join(options[:root], options[:rackup])
          options[:rack_app] = Rack::Builder.parse_file(rackup).first
        end
        if options[:init]
          options[:init] = options[:init].to_java(java.lang.Runnable)
        end
        handler = RackHandler.new(options[:rack_app], options[:path])
        register_options = extract_options(options, WBWeb::RegisterOption)
        @logger.tracef("Registering handler at context path %s", options[:path])
        @web_component.register_handler(handler, register_options)
        handler
      end

      def unmount(path)
        @web_component.unregister(path)
      end

      DEFAULT_MOUNT_SERVLET_OPTIONS = {
        :path => '/'
      }

      def mount_servlet(servlet, options={})
        options = DEFAULT_MOUNT_SERVLET_OPTIONS.merge(options);
        validate_options(options, opts_to_set(WBWeb::RegisterOption) + DEFAULT_MOUNT_SERVLET_OPTIONS.keys)
        @logger.debugf("Mounting servlet %s with options %s on TorqueBox::Web::Server '%s'",
                       servlet, options.inspect, @web_component.name)
        register_options = extract_options(options, WBWeb::RegisterOption)
        @web_component.register_servlet(servlet, register_options)
      end

      def sockjs(context, options={})
        sockjs_server = SockJsServer.new
        # TODO: handle options
        servlet = SockJsServlet.new(sockjs_server)
        mount_servlet(servlet, :path => context)
        sockjs_server
      end

      def start
        @logger.infof("Starting TorqueBox::Web::Server '%s'",
                      @web_component.name)
        @web_component.start
      end

      def stop
        @logger.infof("Stopping TorqueBox::Web::Server '%s'",
                      @web_component.name)
        @web_component.stop
      end


      protected

      def initialize(name, options={})
        @logger = WB.logger('TorqueBox::Web::Server')
        options = DEFAULT_CREATE_OPTIONS.merge(options)
        validate_options(options, DEFAULT_CREATE_OPTIONS.keys)
        create_options = extract_options(options, WBWeb::CreateOption)
        web = WB.find_or_create_component(WBWeb.java_class, name,
                                          create_options)
        @logger.debugf("TorqueBox::Web::Server '%s' has component %s",
                       name, web)
        @web_component = web
      end

    end
  end
end
