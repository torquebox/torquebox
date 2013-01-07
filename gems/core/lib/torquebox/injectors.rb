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

require 'torquebox/registry'

module TorqueBox
  module Injectors

    def self.analyze_and_inject(&block)
      fetch( 'runtime-injection-analyzer' ).analyze_and_inject( block )
    end

    def fetch(something)
      TorqueBox::Registry[something.to_s]
    end
    alias_method :inject, :fetch
    alias_method :__inject__, :fetch
    
    %w{ msc service cdi jndi queue topic }.each do |type|
      define_method("inject_#{type}".to_sym) do |key|
        fetch(key)
      end

      define_method("fetch_#{type}".to_sym) do |key|
        fetch(key)
      end
    end

  end
end
