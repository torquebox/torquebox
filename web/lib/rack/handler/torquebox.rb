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

require 'torquebox-web'
require 'rack/handler'

module Rack
  module Handler
    module TorqueBox

      def self.run(app, options={})
        log_level = 'INFO'
        if options[:Quiet]
          log_level = 'ERROR'
        elsif options[:Verbose]
          log_level = 'DEBUG'
        elsif options[:ReallyVerbose]
          log_level = 'TRACE'
        end
        org.projectodd.wunderboss.WunderBoss.log_level = log_level

        server_options = {
          :host => options[:Host],
          :port => options[:Port],
          :auto_start => false,
          :rack_app => app }
        server = ::TorqueBox::Web::Server.run('default', server_options)
        yield server if block_given?

        server.start
        thread = Thread.current
        Signal.trap("INT") do
          server.stop
          thread.wakeup
        end
        Signal.trap("TERM") do
          server.stop
          thread.wakeup
        end
        sleep
      end

      def self.valid_options
        defaults = ::TorqueBox::Web::Server::DEFAULT_CREATE_OPTIONS
        {
          "Host=HOST" => "Hostname to listen on (default: #{defaults[:host]})",
          "Port=PORT" => "Port to listen on (default: #{defaults[:port]})",
          "Quiet" => "Log only errors",
          "Verbose" => "Log more",
          "ReallyVerbose" => "Log a lot"
        }
      end
    end

    register :torquebox, TorqueBox
  end
end
