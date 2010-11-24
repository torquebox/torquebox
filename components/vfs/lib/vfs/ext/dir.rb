
class Dir

  class << self

    alias_method :open_before_vfs, :open
    alias_method :glob_before_vfs, :glob
    alias_method :mkdir_before_vfs, :mkdir
    alias_method :new_before_vfs, :new

    def open(str,&block)
      #if ( ::File.exist_without_vfs?( str.to_str ) && ! Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( str.to_str ) )
      if ( ::File.exist_without_vfs?( str.to_str ) )
        return open_before_vfs(str,&block)
      end
      #puts "open(#{str})"
      result = dir = VFS::Dir.new( str.to_str )
      #puts "  result = #{result}"
      if block
        begin
          result = block.call(dir)
        ensure
          dir.close
        end
      end
      #puts "open(#{str}) return #{result}"
      result
    end

    def [](pattern)
      self.glob( pattern )
    end

    def glob(pattern,flags=0, &block)
      is_absolute_vfs = false

      str_pattern = pattern.to_str
      #puts "glob(#{str_pattern})"

      segments = str_pattern.split( '/' )

      base_segments = []
      for segment in segments
        if ( segment =~ /[\*\?\[\{\}]/ )
          break
        end
        base_segments << segment
      end

      base = base_segments.join( '/' )

      base.gsub!( /\\(.)/, '\1' )

      #if ( base.empty? || ( ::File.exist_without_vfs?( base ) && ! Java::OrgJbossVirtualPluginsContextJar::JarUtils.isArchive( base ) ) )
      #if ( base.empty? || ( ::File.exist_without_vfs?( base ) ) )
        #puts "doing FS glob"
        #paths = glob_before_vfs( str_pattern, flags, &block )
        #return paths
      #end

      #puts "base= #{base}"

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
        #puts "0 vfs_url=#{vfs_url}"
        starting_point = root = org.jboss.vfs::VFS.child( vfs_url )
        #puts "A starting_point=#{starting_point.path_name}"
        starting_point = root.get_child( child_path ) unless ( child_path.nil? || child_path == '' )
        #puts "B starting_point=#{starting_point.path_name}"
        return [] if ( starting_point.nil? || ! starting_point.exists? )
        child_path = starting_point.path_name
        #puts "child- #{child_path}"
        unless ( child_path =~ %r(/$) )
          child_path = "#{child_path}/"
        end
        child_path = "" if child_path == "/"
        #puts "child_path=#{child_path}"
        #puts "base=#{base}"
        filter = VFS::GlobFilter.new( child_path, matcher )
        #puts "filter is #{filter}"
        paths = starting_point.getChildrenRecursively( filter ).collect{|e|
          #path_name = e.path_name
          path_name = e.getPathNameRelativeTo( starting_point )
          #puts "(collect) path_name=#{path_name}"
          result = ::File.join( base, path_name )
          #puts "(collect) result=#{result}"
          result
        }
        paths.each{|p| block.call(p)} if block
        #puts "Path=#{paths.inspect}"
        paths
      rescue Java::JavaIo::IOException => e
        return []
      end
    end

    def mkdir(path, mode=0777)
      virtual = org.jboss.vfs::VFS.child( path )
      raise "failure" unless virtual.physical_file.mkdir
    rescue Exception
      real_path = path =~ /^vfs:/ ? path[4..-1] : path
      mkdir_before_vfs( real_path, mode )
    end

    def new(string)
      if ( ::File.exist_without_vfs?( string.to_s ) )
        return new_before_vfs( string )
      end
      VFS::Dir.new( string.to_s )
    end
  end
end

