# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
      home = Gem::Specification.find_by_name( 'torquebox-server' )
      home.full_gem_path if home
    rescue Exception => e
      puts "Cannot find torquebox-server: #{e}"
      nil
    end

    def self.jboss_home
      File.join(torquebox_home, 'jboss') if torquebox_home
    end

    def self.jruby_home
      File.expand_path(java.lang.System.getProperty('jruby.home'))
    end
  end
end
