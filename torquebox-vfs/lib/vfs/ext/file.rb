

class File

  class << self

    alias_method :mtime_without_vfs,      :mtime
    alias_method :exist_without_vfs?,     :exist?
    alias_method :directory_without_vfs?, :directory?

    def mtime(filename)
      return mtime_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )
      existing = VFS.first_existing( filename )
      remainder = filename[existing.length..-1]
      is_archive = Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( File.basename( existing ) )
      if ( is_archive )
        prefix = "vfszip://#{Dir.pwd}"
        base = "#{prefix}/#{existing}"
      end
      root = org.jboss.virtual.VFS.root( base )
      file = root.get_child( remainder )
      Time.at( file.last_modified )
    end

    def exist?(filename)
      return true if exist_without_vfs?( filename )

      existing = VFS.first_existing( filename )
      remainder = filename[existing.length..-1]
      is_archive = Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( File.basename( existing ) )
      if ( is_archive )
        prefix = "vfszip://#{Dir.pwd}"
        base = "#{prefix}/#{existing}"
      else
        return false
      end

      root = org.jboss.virtual.VFS.root( base )
      file = root.get_child( remainder )
      return ( ( ! file.nil? ) && file.exists() )
    end

    def directory?(filename)
      return true if directory_without_vfs?( filename )
 
      existing = VFS.first_existing( filename )
      remainder = filename[existing.length..-1]
      is_archive = Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( File.basename( existing ) )
      if ( is_archive )
        prefix = "vfszip://#{Dir.pwd}"
        base = "#{prefix}/#{existing}"
      else
        return false
      end

      root = org.jboss.virtual.VFS.root( base )
      file = root.get_child( remainder )
      return ( ( ! file.nil? ) && ( ! file.isLeaf() ) )
    end

  end

end
