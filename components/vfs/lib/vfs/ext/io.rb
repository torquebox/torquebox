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

require 'vfs/ext/virtual_file'

class IO

  class << self

    #alias_method :open_without_vfs, :open
    alias_method :read_without_vfs, :read
    alias_method :readlines_without_vfs, :readlines
    
    def vfs_open(fd,mode_str='r', &block)
      append   = false
      truncate = false
      write    = false
      read     = false
      create   = false
      case ( mode_str )
        when /r/
          read   = true
          write  = false
          append = false
          create = false
        when /r\+/
          read   = true
          write  = true
          create = false
          append = false
        when /w/
          read     = false
          write    = true
          create   = true
          append   = false
          truncate = true
        when /w\+/
          read   = false
          write  = true
          create = true
          append = false
          truncate = true
        when /a/
          read   = false
          write  = true
          create = true
          append = true
        when /a\+/
          read   = true
          write  = true
          create = true
        when Fixnum
          if ( mode_str & File::RDONLY != 0 )
            read  = true
            write = false
          end
          if ( mode_str & File::WRONLY != 0  )
            read  = false
            write = true
          end
          if ( mode_str & File::RDWR  != 0)
            read  = true
            write = true
          end
          if ( mode_str & File::APPEND  != 0)
            append = true
          end
          if ( mode_str & File::TRUNC  != 0)
            append = false
            truncate = true
          end
          if ( mode_str & File::CREAT  != 0)
            create = true
          end
      end

      # VFS doesn't correctly handle relative paths when
      # retrieving the physical file so expand it
      fd = File.expand_path( fd )
      virtual_file = org.jboss.vfs.VFS.child( fd )

      if ( ! create && ! virtual_file.exists )
        raise Errno::ENOENT
      end

      if ( read && ! write )
        java_in = virtual_file.open_stream()
        ruby_io = java_in.to_io
      elsif ( write && ! read )
        physical_file = virtual_file.physical_file
        java_out = java.io::FileOutputStream.new( physical_file, append )
        ruby_io = java_out.to_io
      elsif ( read && write )
        raise Error.new( "Random-access on VFS not supported" )
      end

      file_io = VFS::Ext::VirtualFile.new( ruby_io, fd )

      if ( block )
        begin
          block.call( file_io )
        ensure
          file_io.close
        end
      else
        return file_io
      end

    end

    def read(name, length=nil, offset=nil)
      if ::File.exist_without_vfs?( name )
        read_without_vfs(name, length, offset)
      else
        vfs_file = vfs_open( name )
        vfs_file.seek( offset ) if offset
        vfs_file.read( length )
      end
    end
    
    # FIXME: this is not ruby 1.9 compliant - the 1.9 signature is:
    # IO.readlines( portname, separator=$/ <, options-for-open> )
    # IO.readlines( portname, limit <, options-for-open> )
    # IO.readlines( portname, separator, limit <, options-for-open> )
    def readlines(name, separator=$/)
      if ::File.exist_without_vfs?( name )
        readlines_without_vfs( name, separator )
      else
        vfs_open( name ).readlines( separator )
      end
    end
  end

end
