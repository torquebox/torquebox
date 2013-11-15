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

require 'wunderboss-rack.jar'

module TorqBox
  class Server

    SERVER_DEFAULT_OPTIONS = {
      :host => 'localhost',
      :port => 8080,
      :log_level => 'INFO'
    }
    APP_DEFAULT_OPTIONS = {
      :context => '/',
      :root => '.',
      :rackup => 'config.ru',
      :rack_app => nil
    }

    def initialize(options)
      options = SERVER_DEFAULT_OPTIONS.merge(options)
      @container = Java::OrgProjectoddWunderboss::WunderBoss.new
      @container.log_level = options[:log_level]
      @container.register_language('ruby', Java::OrgProjectoddWunderbossRuby::RubyLanguage.new)
      @container.register_component('web', Java::OrgProjectoddWunderbossWeb::WebComponent.new)
      @container.register_component('rack', Java::OrgProjectoddWunderbossRubyRack::RackComponent.new)
      @container.configure('web', 'host' => options[:host], 'port' => options[:port].to_s)
      @logger = @container.get_logger('TorqBox::Server')
    end

    def start(options)
      options = APP_DEFAULT_OPTIONS.merge(options)
      @logger.info("TorqBox #{::TorqBox::VERSION} starting...")
      app = @container.new_application('ruby')
      app.start('rack', 'context' => options[:context],
                'root' => options[:root],
                'rackup' => options[:rackup],
                'rack_app' => options[:rack_app])
    end

    def stop
      @logger.info("Stopping TorqBox...")
      @container.stop
    end
  end
end
