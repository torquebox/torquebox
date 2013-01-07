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

require 'java'
require 'rubygems'

module TorqueBox
  class Server

    def self.torquebox_home
      if ((gem_version <=> Gem::Version.new('1.8.9')) < 0)
        home = Gem.searcher.find( 'torquebox-server' )
      else
        home = Gem::Specification.find_by_name( 'torquebox-server' )
      end
      home.full_gem_path if home
    rescue Exception => e
      nil
    end

    def self.jboss_home
      File.join(torquebox_home, 'jboss') if torquebox_home
    end

    def self.jruby_home
      File.expand_path(java.lang.System.getProperty('jruby.home'))
    end

    def self.gem_version
      Gem::Version.new( Gem::VERSION )
    end

    def self.setup_environment
      ENV['TORQUEBOX_HOME'] ||= torquebox_home
      ENV['JBOSS_HOME'] ||= "#{ENV['TORQUEBOX_HOME']}/jboss"
      ENV['JRUBY_HOME'] ||= jruby_home
      ENV['JBOSS_OPTS'] ||= "-Djruby.home=#{jruby_home}"
      %w(JBOSS_HOME JRUBY_HOME).each { |key| puts "[ERROR] #{key} is not set. Install torquebox-server gem or manually set #{key}" unless ENV[key] }
    end

  end

end
