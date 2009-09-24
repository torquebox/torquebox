
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
      existing = VFS.first_existing( name )
      remainder = name[existing.length..-1]
      is_archive = Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( File.basename( existing ) )
      if ( is_archive )
        prefix = "vfszip://#{Dir.pwd}"
        base = "#{prefix}/#{existing}/"
      end
      root = org.jboss.virtual.VFS.root( base )
      file = root.get_child( remainder )
      stream = file.openStream()
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
