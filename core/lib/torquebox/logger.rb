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

require 'logger'
require 'stringio'

module TorqueBox
  class Logger

    java_import org.projectodd.wunderboss::WunderBoss
    java_import org.projectodd.wunderboss::LogbackUtil
    java_import Java::ch.qos.logback.classic.joran.JoranConfigurator
    java_import Java::ch.qos.logback.core.joran.spi::JoranException

    DEFAULT_CATEGORY = 'TorqueBox'.freeze

    STD_LOGGER_LEVELS = {
      ::Logger::DEBUG => 'DEBUG',
      ::Logger::INFO  => 'INFO',
      ::Logger::WARN  => 'WARN',
      ::Logger::ERROR => 'ERROR',
      ::Logger::FATAL => 'FATAL'
    }

    class << self
      attr_reader :log_level

      def log_level=(level)
        @log_level = level
        WunderBoss.log_level = level
      end

      def context
        @context ||= org.slf4j.LoggerFactory.getILoggerFactory
      end

      def configure_with_xml(xml)
        context.reset
        configurator = JoranConfigurator.new
        configurator.context = context
        configurator.do_configure(xml)
      rescue JoranException
        configurator.do_configure(StringIO.new(xml).to_inputstream)
      end
    end

    attr_accessor :formatter

    def initialize(name = DEFAULT_CATEGORY)
      @logger = WunderBoss.logger(name.to_s.gsub('::', '.'))
    end

    def trace?
      @logger.trace_enabled?
    end

    def debug?
      @logger.debug_enabled?
    end

    def info?
      @logger.info_enabled?
    end

    def warn?
      @logger.warn_enabled?
    end

    def error?
      @logger.error_enabled?
    end
    alias_method :fatal?, :error?

    def trace(*params, &block)
      add(:trace, *params, &block)
    end

    def debug(*params, &block)
      add(:debug, *params, &block)
    end

    def info(*params, &block)
      add(:info, *params, &block)
    end

    def warn(*params, &block)
      add(:warn, *params, &block)
    end

    def error(*params, &block)
      add(:error, *params, &block)
    end
    alias_method :fatal, :error

    # Allow our logger to be used for env['rack.errors']
    def puts(message)
      info message.to_s
    end

    def write(message)
      info message.strip
    end

    def flush
    end

    def level
      (@logger.level || @logger.effective_level).to_s
    end

    def level=(new_level)
      if new_level.respond_to?(:to_int)
        new_level = STD_LOGGER_LEVELS[new_level]
      end

      LogbackUtil.set_log_level(@logger, new_level)
    end

    private

    def add(severity, *params)
      message = block_given? ? yield : params.shift
      @logger.send(severity, message, *params)
    end

  end
end
