# Copyright 2016 Red Hat, Inc, and individual contributors.
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

require 'English'

module TorqueBox
  class CLI
    class ShellCommand

      attr_reader :command, :options, :prefix, :output, :exit_status

      def initialize(command, options = {})
        @command     = command
        @options     = options
        @prefix      = options[:prefix]
        @output      = nil
        @exit_status = nil
      end

      def run
        with_blank_rubyopt do
          begin
            run_command
          rescue => e
            print_error(e)
          end
        end
        self
      end

      def succeeded?
        run if exit_status.nil?
        exit_status.success?
      end

      def failed?
        !succeeded?
      end

      def to_s
        @output.to_s
      end

      class << self
        def run(command, options = {})
          new(command, options).run
        end

        def run_jruby(command, options = {})
          run(command, options.merge(:prefix => jruby_prefix))
        end

        def jruby_prefix
          @jruby_prefix ||= [jruby_path, jruby_cli_version, '-S'].join(' ')
        end

        def jruby_path
          @jruby_path ||= begin
            File.join(
              RbConfig::CONFIG['bindir'],
              RbConfig::CONFIG['ruby_install_name']
            )
          end
        end

        def jruby_cli_version
          @jruby_cli_version ||= begin
            unless JRUBY_VERSION >= '9'
              case RUBY_VERSION
              when /^1\.8\./ then '--1.8'
              when /^1\.9\./ then '--1.9'
              when /^2\.0\./ then '--2.0'
              end
            end
          end
        end
      end

      private

      def with_blank_rubyopt
        old_rubyopt = ENV['RUBYOPT']
        begin
          ENV['RUBYOPT'] = ''
          yield if block_given?
        ensure
          ENV['RUBYOPT'] = old_rubyopt
        end
      end

      def run_command
        @output      = `#{prefix} #{command}`
        @exit_status = $CHILD_STATUS
        puts @output
      end

      def print_error(error)
        puts error.message
        puts error.backtrace.join("\n")
      end
    end
  end
end
