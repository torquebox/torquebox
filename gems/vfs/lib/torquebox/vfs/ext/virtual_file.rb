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

require 'delegate'

module TorqueBox
  module VFS
    module Ext
      class VirtualFile < SimpleDelegator

        def initialize(io, path=nil)
          super(io)
          @path = path
        end

        def atime()
          ::File.atime( path )
        end

        def chmod(mode_int)
          ::File.chmod( mode_int, path )
        end

        def chown(owner_int, group_int)
          ::File.chown( owner_int, group_int, path )
        end

        def ctime()
          ::File.ctime( path )
        end

        def flock(locking_constant)
          # not supported
        end

        def lstat()
          ::File.stat( path )
        end

        def mtime()
          ::File.mtime( path )
        end

        def o_chmod(mode_int)
          self.chmod(mode_int)
        end

        def path()
          @path
        end

        def truncate(max_len)
        end

      end
    end
  end
end
