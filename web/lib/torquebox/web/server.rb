require 'torquebox/option_utils'

module TorqueBox
  module Web
    class Server
      include TorqueBox::OptionUtils

      WB = org.projectodd.wunderboss.WunderBoss
      WBWeb = org.projectodd.wunderboss.web.Web
      java_import org.projectodd.wunderboss.rack.RackHandler

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

      DEFAULT_MOUNT_OPTIONS = {
        :root => '.',
        :context_path => '/',
        :static_dir => 'public',
        :init => nil,
        :destroy => nil,
        :rackup => 'config.ru',
        :rack_app => nil
      }

      def mount(options={})
        options = DEFAULT_MOUNT_OPTIONS.merge(options)
        validate_options(options, enum_to_set(WBWeb::RegisterOption) + DEFAULT_MOUNT_OPTIONS.keys)
        @logger.debugf("Mounting context path %s with options %s on TorqueBox::Web::Server '%s'",
                       options[:context_path], options.inspect, @web_component.name)
        if options[:rack_app].nil?
          require 'rack'
          rackup = File.join(options[:root], options[:rackup])
          options[:rack_app] = Rack::Builder.parse_file(rackup).first
        end
        if options[:init]
          options[:init] = options[:init].to_java(java.lang.Runnable)
        end
        handler = RackHandler.new(options[:rack_app], options[:context_path])
        register_options = extract_options(options, WBWeb::RegisterOption)
        @logger.tracef("Registering handler at context path %s", options[:context_path])
        @web_component.register_handler(handler, register_options)
        handler
      end

      def unmount(context_path)
        @web_component.unregister(context_path)
      end

      def mount_servlet
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
        validate_options(options, enum_to_set(WBWeb::CreateOption) + DEFAULT_CREATE_OPTIONS.keys)
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
