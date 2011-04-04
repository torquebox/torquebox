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
  module VFS
    module File
      class Stat 
        def initialize(virtual_file)
          @virtual_file = virtual_file
        end

        def cmp(other)
          @virtual_file.last_modified <=> other.last_modified
        end

        def atime()
          Time.at( @virtual_file.last_modified )
        end

        def blksize
          nil
        end

        def blockdev?
          false
        end

        def blocks
          nil
        end

        def chardev?
          true
        end

        def ctime
          Time.at( @virtual_file.last_modified )
        end

        def dev
          nil
        end

        def dev_major
          nil
        end

        def dev_minor
          nil
        end

        def directory?
          @virtual_file.exists && ! @virtual_file.is_leaf 
        end

        def executable?
          false
        end

        def executable_real?
          false
        end

        def file?
          true
        end

        def ftype
          return 'file' if @virtual_file.is_leaf
          'directory'
        end

        def gid
          nil
        end

        def grpowned?
          false
        end

        def ino
          nil
        end

        def mode
          0x444
        end

        def mtime
          Time.at( @virtual_file.getLastModified() / 1000 )
        end

        def nlink
          1
        end

        def owned?
          false
        end

        def pipe?
          false
        end

        def rdev
          nil
        end

        def rdev_major
          nil
        end

        def rdev_minor
          nil
        end

        def readable?
          true
        end

        def readable_real?
          true
        end

        def setgid?
          false
        end

        def setuid?
          false
        end

        def size
          @virtual_file.size
        end

        def socket?
          false
        end

        def sticky?
          false
        end

        def symlink?
          false
        end

        def uid
          nil
        end

        def writable?
          begin
            physical_file = @virtual_file.physical_file
            physical_file.canWrite
          rescue => e
            false
          end
        end

        def writable_real?
          writable?
        end

        def zero?
          self.size == 0
        end

      end

    end
  end
end
