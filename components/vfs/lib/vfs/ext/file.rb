

class File

  class << self

    alias_method :open_without_vfs,        :open
    alias_method :mtime_without_vfs,       :mtime
    alias_method :stat_without_vfs,        :stat
    alias_method :exist_without_vfs?,      :exist?
    alias_method :directory_without_vfs?,  :directory?
    alias_method :file_without_vfs?,       :file?
    alias_method :expand_path_without_vfs, :expand_path
    alias_method :unlink_without_vfs,      :unlink


    def open(fname,mode_str='r', flags=nil, &block)
      if ( Fixnum === fname )
        return File.open_without_vfs( fname, mode_str, &block )
      end
      unless ( fname.to_s =~ /^vfs:/ )
        return File.open_without_vfs(fname, mode_str, flags, &block )
      end
      IO.vfs_open( fname.to_s, mode_str, &block )
    end

    def expand_path(*args)
      if ( args[1] && args[1].to_s =~ /^vfs:/ )
        return "#{expand_path_without_vfs(args[0], args[1].to_s[4..-1])}" 
      end
      return args[0].to_s.dup if ( args[0] =~ /^vfs:/ )
      expand_path_without_vfs(*args) 
    end

    def unlink(*file_names)
      file_names.each do |file_name|
        if ( file_name.to_s =~ /^vfs:/ )
          virtual_file = org.jboss.vfs::VFS.child( file_name.to_s ) 
          raise Errno::ENOENT.new unless virtual_file.exists()
          virtual_file.delete 
        else 
          unlink_without_vfs( file_name )
        end
      end
      file_names.size
    end

    alias_method :delete, :unlink

    def mtime(filename)
      return mtime_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      raise Errno::ENOENT.new unless vfs_url

      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
      virtual_file = virtual_file.get_child( child_path ) if child_path

      Time.at( virtual_file.getLastModified() / 1000 )
    end

    def stat(filename)
      return stat_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      raise Errno::ENOENT.new nil unless vfs_url

      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
      virtual_file = virtual_file.get_child( child_path ) if child_path

      VFS::File::Stat.new( virtual_file )
    end

    def exists?(filename)
      exist?(filename)
    end

    def exist?(filename)
      return true if exist_without_vfs?( filename )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      begin
        virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
        virtual_file = virtual_file.get_child( child_path ) if child_path
  
        return ( ( ! virtual_file.nil? ) && virtual_file.exists() )
      rescue Java::JavaIo::IOException => e
        return false
      end
    end

    def writable?(filename)
      stat = stat(filename)
      return stat.writable? if stat
      false
    end

    def directory?(filename)
      return true if directory_without_vfs?( filename )
 
      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      begin
        virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
        virtual_file = virtual_file.get_child( child_path ) if child_path
  
        return ( ( ! virtual_file.nil? ) && ( virtual_file.isDirectory() ) )
      rescue Java::JavaIo::IOException => e
        return false
      end
    end

    def file?(filename)
      return true if file_without_vfs?( filename )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      begin
        virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
        virtual_file = virtual_file.get_child( child_path ) if child_path

        return ( ( ! virtual_file.nil? ) && ( virtual_file.isLeaf() ) )
      rescue Java::JavaIo::IOException => e
        return false
      end
    end

  end

end
