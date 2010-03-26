
class IO

  class << self

    #alias_method :open_without_vfs, :open
    alias_method :read_without_vfs, :read

    def vfs_open(fd,mode_str='r', &block)
      unless ( (String === fd) && ( fd =~ /^vfszip:/ || fd =~ /^vfsfile:/ ) )
        return open_without_vfs(fd, mode_str, &block )
      end
      file = org.jboss.vfs.VFS.child( fd )
      raise Errno::ENOENT unless file
      stream = file.openStream()
      io = stream.to_io 
      result = io
      ( result = block.call( io ) ) if block
      result
    end

    def read(name, length=nil, offset=nil)
      return read_without_vfs(name, length, offset) if ::File.exist_without_vfs?( name )

      puts "IO#read(#{name})"
      vfs_url, child_path = VFS.resolve_within_archive(name)
      puts "vfs_url=#{vfs_url}"
      puts "child_path=#{child_path}"
      raise ::Errno::ENOENT unless vfs_url

      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
      virtual_file = virtual_file.getChild( child_path ) if child_path
      raise ::Errno::ENOENT unless virtual_file

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
