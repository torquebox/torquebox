# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

class Dir

  class << self

    alias_method :open_before_vfs, :open
    alias_method :glob_before_vfs, :glob
    alias_method :mkdir_before_vfs, :mkdir
    alias_method :rmdir_before_vfs, :rmdir
    alias_method :chdir_before_vfs, :chdir
    alias_method :new_before_vfs, :new
    alias_method :entries_before_vfs, :entries
    alias_method :foreach_before_vfs, :foreach

    # 1.8: open( dirname )
    # 1.9: open( dirname, <, :encoding => enc> )
    # We currently ignore the encoding.
    def open(str, options = nil, &block)
      if ( ::File.exist_without_vfs?( str ) )
        return open_before_vfs(str,&block)
      end
      #puts "open(#{str})"
      result = dir = TorqueBox::VFS::Dir.new( str )
      #puts "  result = #{result}"
      unless result.exists?
        return open_before_vfs(str,&block)
      end
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

      #str_pattern = "#{pattern}"
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

      vfs_url, child_path = TorqueBox::VFS.resolve_within_archive( base )
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
        filter = TorqueBox::VFS::GlobFilter.new( child_path, matcher )
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

    def chdir(*args, &block)
      raise "You shouldn't use chdir, but if you must, pass a block!" unless block_given?
      chdir_before_vfs( *args.map{ |x| File.name_without_vfs(x) }, &block )
    end

    def rmdir(path)
      name = File.name_without_vfs(path)
      rmdir_before_vfs(name)
    end
    alias_method :unlink, :rmdir
    alias_method :delete, :rmdir

    def mkdir(path, mode=0777)
      mkdir_before_vfs( File.name_without_vfs(path), mode )
    rescue Errno::ENOTDIR => e
      path = TorqueBox::VFS.writable_path_or_error( File.path_to_str(path), e )
      mkdir_before_vfs( path, mode )
    rescue Errno::ENOENT => e
      path = TorqueBox::VFS.writable_path_or_error( File.path_to_str(path), e )
      mkdir_before_vfs( path, mode )
    end

    # 1.8: new( dirname )
    # 1.9: new( dirname, <, :encoding => enc> )
    # We currently ignore the encoding.
    def new(string, options = nil)
      if ( ::File.exist_without_vfs?( string ) )
        return new_before_vfs( string )
      end
      TorqueBox::VFS::Dir.new( string )
    end

    # 1.9 has an optional, undocumented options arg that appears to be
    # used for encoding. We'll ignore it for now, since JRuby does as
    # well. (see org.jruby.RubyDir.java)
    def entries(path, options = {})
      if ( ::File.exist_without_vfs?( path ) )
        return entries_before_vfs(path)
      end
      vfs_dir = org.jboss.vfs::VFS.child( File.path_to_str(path) )
      # Delegate to original entries if passed a nonexistent file
      unless vfs_dir.exists?
        return entries_before_vfs( path )
      end
      [ '.', '..' ] + vfs_dir.children.collect{|e| e.name }
    end

    def foreach(path, &block)
      enum = entries(path).each(&block)
      block_given? ? nil : enum
    end

  end
end

