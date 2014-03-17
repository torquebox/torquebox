module TorqueBox
  module Web
    class Server

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
        :rackup => 'config.ru',
        :rack_app => nil
      }
      def mount(options={})
        options = DEFAULT_MOUNT_OPTIONS.merge(options)
        validate_options(options, DEFAULT_MOUNT_OPTIONS)
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
        validate_options(options, DEFAULT_CREATE_OPTIONS)
        create_options = extract_options(options, WBWeb::CreateOption)
        web = WB.find_or_create_component(WBWeb.java_class, name,
                                          create_options)
        @logger.debugf("TorqueBox::Web::Server %s has component %s",
                       name, web)
        @web_component = web
      end

      def validate_options(options, valid_options)
        valid_keys = valid_options.keys
        options.keys.each do |key|
          unless valid_keys.include?(key)
            raise ArgumentError.new("#{key} is not a valid option")
          end
        end
      end

      def extract_options(options, enum)
        enum_values = enum.values.inject({}) do |hash, entry|
          hash[entry.value] = entry
          hash
        end
        extracted_options = {}
        options.each_pair do |key, value|
          key = key.to_s
          if enum_values.include?(key)
            extracted_options[enum_values[key]] = value
          end
        end
        extracted_options
      end
    end
  end
end
