

class File

  class << self

    alias_method :mtime_without_vfs,      :mtime
    alias_method :stat_without_vfs,       :stat
    alias_method :exist_without_vfs?,     :exist?
    alias_method :directory_without_vfs?, :directory?
    alias_method :file_without_vfs?,      :file?

    def mtime(filename)
      return mtime_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return nil unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )

      child = virtual_file.get_child( child_path )

      Time.at( child.getLastModified() / 1000 )
    end

    def stat(filename)
      return stat_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return nil unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      child = virtual_file.get_child( child_path )

      VFS::File::Stat.new( child )
    end

    def exist?(filename)
      return true if exist_without_vfs?( filename )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      child = virtual_file.get_child( child_path )

      return ( ( ! child.nil? ) && child.exists() )
    end

    def directory?(filename)
      return true if directory_without_vfs?( filename )
 
      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      child = virtual_file.get_child( child_path )
  
      return ( ( ! child.nil? ) && ( ! child.isLeaf() ) )
    end

    def file?(filename)
      return true if file_without_vfs?( filename )

      vfs_url, child_path = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      child = virtual_file.get_child( child_path )

      return ( ( ! child.nil? ) && ( child.isLeaf() ) )
    end

  end

end
