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


module TorqueBox
  class Logger

    DEFAULT_CATEGORY = 'TorqueBox'.freeze

    class << self
      attr_reader :log_level

      def log_level=(level)
        @log_level = level
        org.projectodd.wunderboss.WunderBoss.log_level = level
      end

      def logger
        @logger ||= new
      end
    end

    def initialize(name = nil)
      category = name || DEFAULT_CATEGORY
      @logger = org.projectodd.wunderboss.WunderBoss.logger(category.to_s.gsub('::', '.'))
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

    [:trace, :debug, :info, :warn, :error].each do |severity|
      define_method(severity) do |*params, &block|
        add(severity, *params, &block)
      end
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

    private

    def add(severity, *params)
      message = block_given? ? yield : params.shift
      @logger.send(severity, message, *params)
    end

  end
end
