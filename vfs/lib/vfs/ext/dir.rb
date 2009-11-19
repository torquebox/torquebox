
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
      #puts "============"
      #puts "glob(#{pattern})"

      is_absolute_vfs = false

      segments = pattern.split( '/' )

      base_segments = []
      for segment in segments
        if ( segment =~ /[\*\?\[\{]/ )
          break
        end
        base_segments << segment
      end

      base = base_segments.join( '/' )

      if ( ::File.exist_without_vfs?( base ) && ! Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( base ) )
        paths = glob_before_vfs( pattern )
        return paths
      end

      #puts "base=#{base}"
      vfs_url, child_path = VFS.resolve_within_archive( base )
      #puts "vfs_url=#{vfs_url}"
      #puts "child_path=#{child_path}"

      return []       if vfs_url.nil?
      #puts "segments.size==base_segments.size? #{segments.size == base_segments.size}"
      return [ base ] if segments.size == base_segments.size

      matcher_segments = segments - base_segments
      matcher = matcher_segments.join( '/' )
      #puts "matcher [#{matcher}]"

      begin
        starting_point = root = org.jboss.virtual.VFS.root( vfs_url )
        starting_point = root.get_child( child_path ) unless ( child_path.nil? || child_path == '' )
        #puts "starting_point=#{starting_point.path_name}"
        return [] if ( starting_point.nil? || ! starting_point.exists? )
        child_path = starting_point.path_name
        unless ( child_path =~ %r(/$) )
          child_path = "#{child_path}/"
        end
        child_path = "" if child_path == "/"
        #puts "child_path=#{child_path}"
        end_index = if child_path == "" then 1 else child_path.length end
        #end_index = child_path.length if child_path != ""
        prefix = base[0..-(end_index)]
        #puts "prefix=#{prefix}"
        paths = starting_point.children_recursively( VFS::GlobFilter.new( child_path, matcher ) ).collect{|e| 
          path_name = e.path_name
          #puts "(collect) path_name=#{path_name}"
          result = ::File.join( prefix, path_name )
          #puts "(collect) result=#{path_name}"
          result
        }
        paths
      rescue Java::JavaIo::IOException => e
        return []
      end
    end

  end
end 

