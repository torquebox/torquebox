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

require 'torquebox/configuration'

module TorqueBox
  CONFIGURATION_ROOT = '<root>'
  
  def self.configure(&block)
    config = Thread.current[:torquebox_config]
    entry_map = Thread.current[:torquebox_config_entry_map]
    Configuration::Entry.new( CONFIGURATION_ROOT, config, entry_map, :allow_block => true ).
      process( nil, &block )
    config
  end
     
end
