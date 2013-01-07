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
module TorqueBox
  class ServiceRegistry

    def self.service_registry=(service_registry)
      @service_registry = service_registry 
      blocks.keys.each do |name|
        blocks.delete(name).each do |block|
          lookup name, &block
        end
      end
      registry
    end

    # blocks are not allowed on the method named :[]
    def self.[](name)
      self.lookup(name)
    end

    def self.lookup(name, &block)
      if registry.nil?
        self.blocks[name] << block
        nil
      else
        name = TorqueBox::ServiceRegistry.service_name_for( name ) if name.is_a? String
        entry = registry.getService( name )
        return nil unless entry
        if block_given?
          yield entry.getValue()
        else
          entry.getValue()
        end
      end
    end

    def self.blocks
      @blocks ||= Hash.new{|h, k| h[k] = []}
    end

    def self.service_name_for( name )
      Java::org.jboss.msc.service.ServiceName.parse( name )
    end

    def self.registry
      @service_registry
    end

  end

end

