
class IO

  class << self

    alias_method :open_without_vfs, :open
    alias_method :read_without_vfs, :read

    def open(fd,mode_str='r')
      file = org.jboss.virtual.VFS.root( fd )
      stream = file.openStream()
      io = stream.to_io 
      if ( block_given? )
        begin
          yield( io )
        ensure
          io.close()
        end
      end
      io
    end

    def read(name, length=nil, offset=nil)
      return read_without_vfs(name, length) if File.exist_without_vfs?( name )

      vfs_url = VFS.resolve_within_archive(name)
      return nil unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      return nil unless virtual_file

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
