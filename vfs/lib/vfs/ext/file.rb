

class File

  class << self

    alias_method :mtime_without_vfs,      :mtime
    alias_method :stat_without_vfs,       :stat
    alias_method :exist_without_vfs?,     :exist?
    alias_method :directory_without_vfs?, :directory?

    def mtime(filename)
      return mtime_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )

      vfs_url = VFS.resolve_within_archive(filename)
      return nil unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      return nil unless virtual_file
      Time.at( virtual_file.getLastModified() / 1000 )
    end

    def stat(filename)
      return stat_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )

      vfs_url = VFS.resolve_within_archive(filename)
      return nil unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      return nil unless virtual_file

      VFS::File::Stat.new( virtual_file )
    end

    def exist?(filename)
      return true if exist_without_vfs?( filename )

      vfs_url = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      return false unless virtual_file
      true
    end

    def directory?(filename)
      return true if directory_without_vfs?( filename )
 
      vfs_url = VFS.resolve_within_archive(filename)
      return false unless vfs_url

      virtual_file = Java::OrgJbossVirtual::VFS.root( vfs_url )
      return false unless virtual_file
  
      !virtual_file.isLeaf()
    end

  end

end
