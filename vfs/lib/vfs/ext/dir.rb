
class Dir

  class << self

    #alias_method :open_before_vfs, :open
    alias_method :glob_before_vfs, :glob

    def open(str)
      result = dir = VFS::Dir.new( str )
      if block_given?
        begin
          result = yield( dir )
        ensure
          dir.close 
        end
      end
      result
    end

    def [](pattern)
      self.glob( pattern )
    end

    def glob(pattern,flags=nil)
      segments = pattern.split( '/' )

      base_segments = []
      for segment in segments
        if ( segment =~ /[\*\?\[\{]/ )
          break
        end
        base_segments << segment
      end

      base = base_segments.join( '/' )

      if ( ::File.exist_without_vfs?( base ) && ::File.directory_without_vfs?( base ) )
        return glob_before_vfs( pattern )
      end

      vfs_base = VFS.resolve_within_archive( base )

      return []       if vfs_base.nil?
      return [ base ] if segments.size == base_segments.size

      matcher_segments = segments - base_segments
      matcher = matcher_segments.join( '/' )

      root = org.jboss.virtual.VFS.root( vfs_base )
      paths = root.children_recursively( VFS::GlobFilter.new( matcher ) ).collect{|e| 
        "#{base}/#{e.path_name}"
      }
      paths
    end

  end
end 

