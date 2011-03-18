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

module TorqueBox
  module Injectors

    def mc name
      lookup_injection 'mc', name
    end

    def cdi klass, qualifiers = {}
      lookup_injection 'cdi', klass.to_s
    end

    def jndi name
      lookup_injection 'jndi', name
    end

    def log name
      lookup_injection 'log', name
    end

    def lookup_injection type, name
      self.class.const_get("TORQUEBOX_INJECTION_REGISTRY").get(type, name)
    rescue
      STDERR.puts "Unable to inject '#{type}:#{name}': #{$!}"
    end

  end
end
