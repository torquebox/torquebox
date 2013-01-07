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

# DataMapper::Resource uses Procs for default values and Marshal.dump 
# does not like that. So, when sending or receiving a DataMapper::Resource
# we need to override _dump and _load to just serialize the ID and
# class name of the resource, and use Resource.get to _load it.
module TorqueBox
  module Messaging
    module DataMapper

      def self.included(base)
        base.extend(ClassMethods)
      end
      
      def _dump( level )
        [id, self.class].join(':')
      end

      module ClassMethods
        def _load( string )
          id, clazz = string.split(':')
          Kernel.const_get(clazz).get(id)
        end
      end
    end
  end
end
