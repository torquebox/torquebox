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

require 'torquebox/service_registry'

module TorqueBox
  class Kernel

    def self.kernel=(kernel)
      $stderr.puts "[WARNING] >>> TorqueBox::Kernel is deprecated. Please begin using TorqueBox::ServiceRegistry instead."
      # is this even reasonable?
      TorqueBox::ServiceRegistry.service_registry = kernel
    end

    # blocks are not allowed on the method named :[]
    def self.[](name)
      $stderr.puts "[WARNING] >>> TorqueBox::Kernel is deprecated. Please begin using TorqueBox::ServiceRegistry instead."
      self.lookup(name)
    end

    def self.lookup(name, &block)
      $stderr.puts "[WARNING] >>> TorqueBox::Kernel is deprecated. Please begin using TorqueBox::ServiceRegistry instead."
      TorqueBox::ServiceRegistry.lookup(name, &block)
    end

    def self.blocks
      $stderr.puts "[WARNING] >>> TorqueBox::Kernel is deprecated. Please begin using TorqueBox::ServiceRegistry instead."
      TorqueBox::ServiceRegistry.blocks
    end

  end
end
