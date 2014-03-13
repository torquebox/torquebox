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

require 'rack'
require 'wunderboss-torquebox'

module TorqueBox
  class Server

    java_import org.projectodd.wunderboss.WunderBoss
    java_import org.projectodd.wunderboss.torquebox.RackHandler
    java_import org.projectodd.wunderboss.web.Web

    attr_reader :options

    DEFAULT_OPTIONS = {
      :host => 'localhost',
      :port => 8080,
      :log_level => 'INFO',
      :root => '.',
      :context_path => '/',
      :rackup => 'config.ru',
      :rack_app => nil
    }

    def initialize(options)
      @options = DEFAULT_OPTIONS.merge(options)
      WunderBoss.put_option('root', @options[:root])
      WunderBoss.log_level = @options[:log_level]
      @logger = WunderBoss.logger('TorqueBox::Server')
    end

    def start
      @logger.info("TorqueBox #{::TorqueBox::VERSION} starting...")
      if @options[:rack_app].nil?
        rackup = File.join(@options[:root], @options[:rackup])
        @options[:rack_app] = Rack::Builder.parse_file(rackup)[0]
      end
      component_options = {
        Web::CreateOption::HOST => @options[:host],
        Web::CreateOption::PORT => @options[:port].to_s
      }
      @web = WunderBoss.find_or_create_component('web', "default",
                                                 component_options)
      handler = RackHandler.new(@options[:rack_app], @options[:context_path])
      handler_options = {
        Web::RegisterOption::CONTEXT_PATH => @options[:context_path],
        Web::RegisterOption::STATIC_DIR => File.join(@options[:root], 'public')
      }
      @web.registerHandler(handler, handler_options)
    end

    def stop
      @logger.info("Stopping TorqueBox...")
      if @web
        @web.unregister(@options[:context_path])
        @web.stop
      end
    end
  end
end
