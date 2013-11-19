# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

require 'logger'

module TorqueBox

  # @api private
  class FallbackLogger < ::Logger

    attr_accessor :formatter

    def initialize name = nil
      super(ENV['TORQUEBOX_FALLBACK_LOGFILE'] || $stderr)
      @category = name || (TORQUEBOX_APP_NAME if defined? TORQUEBOX_APP_NAME) || "TorqueBox"
      @formatter = ::Logger::Formatter.new
    end

    def add(severity, message, progname, &block)
      if ( message.nil? && block.nil? )
        message = progname
        progname = @category
      end
      message = progname if message.nil?
      super( severity, message, @category, &block )
    end

    # Allow our logger to be used for env['rack.errors']
    def puts(message)
      info message.to_s
    end
    def write(message)
      info message.strip
    end
    def flush
    end
  end

  begin
    org.jboss.logging::Logger
  rescue ::NameError
    Logger = FallbackLogger
    return
  end

  class Logger

    attr_accessor :formatter

    def initialize name = nil
      category = name || (TORQUEBOX_APP_NAME if defined? TORQUEBOX_APP_NAME) || "TorqueBox"
      @logger = org.jboss.logging::Logger.getLogger( category.to_s.gsub('::','.') )
      @formatter = ::Logger::Formatter.new
    end

    [:warn?, :error?, :fatal?].each do |method|
      define_method(method) { true }
    end

    def info?
      @logger.info_enabled?
    end

    def debug?
      @logger.debug_enabled?
    end

    def trace?
      @logger.trace_enabled?
    end

    # The minimum log level to actually log, with debug being the lowest
    # and fatal the highest
    attr_accessor :level

    def add(severity, message, progname, &block)
      severities = ['debug', 'info', 'warn', 'error', 'fatal']
      # default to warn for unknown log level since jboss logger
      # doesn't support unknown
      delegate = severity > (severities.length - 1) ? 'warn' : severities[severity]
      params = params_for_logger([message, progname], block)
      @logger.send(delegate, *params)
    end

    # @!method debug(message)
    #   @param [String] message The message to log
    # @!method info(message)
    #   @param [String] message The message to log
    # @!method warn(message)
    #   @param [String] message The message to log
    # @!method error(message)
    #   @param [String] message The message to log
    # @!method fatal(message)
    #   @param [String] message The message to log
    def method_missing(method, *args, &block)
      delegate = method
      self.class.class_eval do
        define_method(method) do |*a, &b|
          params = params_for_logger(a, b)
          params = [""] if params.empty?
          @logger.send(delegate, *params)
        end
      end
      self.send(method, *args, &block)
    end

    # Allow our logger to be used for env['rack.errors']
    def puts(message)
      info message.to_s
    end
    def write(message)
      info message.strip
    end
    def flush
    end

    private

    def params_for_logger(args, block)
      [ args[0] || (block && block.call) ].compact
    end

  end
end
