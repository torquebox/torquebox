
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
        paths = glob_before_vfs( pattern )
        return paths
      end

      vfs_url, child_path = VFS.resolve_within_archive( base )

      return []       if vfs_url.nil?
      return [ base ] if segments.size == base_segments.size

      matcher_segments = segments - base_segments
      matcher = matcher_segments.join( '/' )

      begin
        starting_point = root = org.jboss.virtual.VFS.root( vfs_url )
        starting_point = root.get_child( child_path ) unless ( child_path.nil? || child_path == '' )
        return [] if ( starting_point.nil? || ! starting_point.exists? )
        child_path = starting_point.path_name
        paths = starting_point.children_recursively( VFS::GlobFilter.new( child_path, matcher ) ).collect{|e| 
          path_name = e.path_name
          result = File.join( base[0..-(child_path.length+1)], path_name )
          result
        }
        paths
      rescue Java::JavaIo::IOException => e
        return []
      end
    end

  end
end 

