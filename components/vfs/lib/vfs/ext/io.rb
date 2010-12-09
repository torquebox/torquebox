
require 'vfs/ext/virtual_file'

class IO

  class << self

    #alias_method :open_without_vfs, :open
    alias_method :read_without_vfs, :read

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
      return read_without_vfs(name, length, offset) if ::File.exist_without_vfs?( name )

      if ( name =~ /^\// || name =~ /^vfs:\// )
        full_path = name
      else
        full_path = File.join( Dir.pwd, name )
      end
      virtual_file = org.jboss.vfs.VFS.child( full_path )
      raise ::Errno::ENOENT.new( "#{name} (#{virtual_file})" ) unless virtual_file.exists()

      stream = virtual_file.openStream()
      io = stream.to_io
      begin
        s = io.read
      ensure
        io.close()
      end
      s
    end
  end

end
