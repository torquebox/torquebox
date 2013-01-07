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

  class FallbackLogger < ::Logger

    def initialize name = nil
      super( $stderr )
      @category = name || (TORQUEBOX_APP_NAME if defined? TORQUEBOX_APP_NAME) || "TorqueBox"
    end

    def add(severity, message, progname, &block)
      if ( message.nil? && block.nil? )
        message = progname
        progname = @category
      end
      message = progname if message.nil?
      super( severity, message, @category, &block )
    end
  end 

  begin
    org.jboss.logging::Logger
  rescue ::NameError
    Logger = FallbackLogger
    return
  end

  class Logger

    def initialize name = nil
      category = name || (TORQUEBOX_APP_NAME if defined? TORQUEBOX_APP_NAME) || "TorqueBox"
      @logger = org.jboss.logging::Logger.getLogger( category.to_s.gsub('::','.') )
    end

    [:warn?, :error?, :fatal?].each do |method|
      define_method(method) { true }
    end

    attr_accessor :level

    def add(severity, message, progname, &block)
      severities = ['debug', 'info', 'warn', 'error', 'fatal']
      # default to warn for unknown log level since jboss logger
      # doesn't support unknown
      delegate = severity > (severities.length - 1) ? 'warn' : severities[severity]
      params = params_for_logger([message, progname], block)
      @logger.send(delegate, *params)
    end

    def method_missing(method, *args, &block)
      delegate = method
      is_boolean = false
      if method.to_s.end_with?('?')
        delegate = "#{method.to_s.chop}_enabled?".to_sym
        is_boolean = true
      end
      self.class.class_eval do
        define_method(method) do |*a, &b|
          params = params_for_logger(a, b)
          params = [""] if params.empty? && !is_boolean
          @logger.send(delegate, *params)
        end
      end
      self.send(method, *args, &block)
    end

    # Make the Logger compatible with Rack::CommonLogger
    #
    def write(message)
      info message.strip
    end

    private

    def params_for_logger(args, block)
      [ args[0] || (block && block.call) ].compact
    end

  end
end
