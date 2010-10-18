

class File

  class << self

    alias_method :open_without_vfs,        :open
    alias_method :mtime_without_vfs,       :mtime
    alias_method :stat_without_vfs,        :stat
    alias_method :exist_without_vfs?,      :exist?
    alias_method :directory_without_vfs?,  :directory?
    alias_method :dirname_without_vfs,     :dirname
    alias_method :file_without_vfs?,       :file?
    alias_method :expand_path_without_vfs, :expand_path
    alias_method :unlink_without_vfs,      :unlink
    alias_method :readable_without_vfs?,   :readable?
    alias_method :chmod_without_vfs,       :chmod
    alias_method :new_without_vfs,         :new

    def open(fname,mode_str='r', flags=nil, &block)
      if ( Fixnum === fname )
        return File.open_without_vfs( fname, mode_str, &block )
      end
      unless ( vfs_path?(fname) )
        return File.open_without_vfs(fname, mode_str, flags, &block )
      end
      if ( File.exist_without_vfs?( name_without_vfs(fname) ) )
        return File.open_without_vfs( name_without_vfs(fname), mode_str, flags, &block )
      end
      self.vfs_open( fname.to_s, mode_str, &block )
    end

    def expand_path(*args)
      return args[0].to_s.dup if ( vfs_path?(args[0]) )
      if ( vfs_path?(args[1]) )
        expanded = expand_path_without_vfs(args[0], name_without_vfs(args[1].to_s))
        return "vfs:#{expanded}"
      end
      expand_path_without_vfs(*args)
    end

    def readable?(filename)
      readable_without_vfs? name_without_vfs(filename)
    end

    def unlink(*file_names)
      file_names.each do |file_name|
        if ( vfs_path?(file_name) )
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

    def chmod(mode_int, *files)
      files.each do |name|
        chmod_without_vfs( mode_int, name_without_vfs(name) )
      end
    end

    def new(*args, &block)
      fname = args.size > 0 ? args[0] : nil
      if ( Fixnum === fname )
        return self.new_without_vfs( *args, &block )
      end
      unless ( vfs_path?(fname) )
        return self.new_without_vfs( *args, &block )
      end
      if ( File.exist_without_vfs?( name_without_vfs(fname) ) )
        args[0] = name_without_vfs(fname)
        return File.new_without_vfs( *args, &block )
      end
      # File.new doesn't pass a block through to the opened file
      IO.vfs_open( *args )
    end

    def dirname(filename)
      dirname = dirname_without_vfs(name_without_vfs(filename))
      vfs_path?(filename) ? "vfs:#{dirname}" : dirname
    end

    def name_without_vfs(filename)
      name = filename.to_s.gsub("\\", "/")
      vfs_path?(name) ? name[4..-1] : name
    end

    def vfs_path?(path)
      path.to_s =~ /^vfs:/
    end
  end

end
