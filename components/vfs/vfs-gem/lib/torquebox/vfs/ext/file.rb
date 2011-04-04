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

require 'torquebox/vfs/debug_filter'
require 'torquebox/vfs/ext/tempfile'

class File

  class << self

    alias_method :open_without_vfs,        :open
    alias_method :mtime_without_vfs,       :mtime
    alias_method :size_without_vfs,        :size
    alias_method :size_without_vfs?,       :size?
    alias_method :stat_without_vfs,        :stat
    alias_method :lstat_without_vfs,       :lstat
    alias_method :exist_without_vfs?,      :exist?
    alias_method :directory_without_vfs?,  :directory?
    alias_method :dirname_without_vfs,     :dirname
    alias_method :file_without_vfs?,       :file?
    alias_method :expand_path_without_vfs, :expand_path
    alias_method :unlink_without_vfs,      :unlink
    alias_method :readable_without_vfs?,   :readable?
    alias_method :chmod_without_vfs,       :chmod
    alias_method :chown_without_vfs,       :chown
    alias_method :utime_without_vfs,       :utime
    alias_method :new_without_vfs,         :new
    alias_method :rename_without_vfs,      :rename
    alias_method :join_without_vfs,        :join

    def open(fname,mode_str='r', flags=nil, &block)
      if ( Fixnum === fname )
        return open_without_vfs( fname, mode_str, &block )
      end
      unless ( vfs_path?(fname) )
        return open_without_vfs(fname, mode_str, flags, &block )
      end
      if ( File.exist_without_vfs?( name_without_vfs(fname) ) )
        return open_without_vfs( name_without_vfs(fname), mode_str, flags, &block )
      end
      self.vfs_open( fname.to_s, mode_str, &block )
    end

    def expand_path(*args)
      return args[0].to_s.dup if ( vfs_path?(args[0]) )
      if ( vfs_path?(args[1]) )
        expanded = expand_path_without_vfs(args[0], name_without_vfs(args[1].to_s))
        return TorqueBox::VFS.resolve_path_url(expanded)
      end
      expand_path_without_vfs(*args)
    end

    def readable?(filename)
      real_name = name_without_vfs( filename )
      return readable_without_vfs?( real_name ) if exist_without_vfs?( real_name )

      virtual_file = virtual_file( filename )
      # VirtualFile has no readable? so assume we can read it if it exists
      !virtual_file.nil? && virtual_file.exists?
    end

    def unlink(*file_names)
      file_names.each do |file_name|
        if ( vfs_path?(file_name) )
          virtual_file = org.jboss.vfs::VFS.child( file_name.to_s )
          raise Errno::ENOENT.new(file_name) unless virtual_file.exists()
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

      vfs_url, child_path = TorqueBox::VFS.resolve_within_archive(filename)
      raise Errno::ENOENT.new(filename) unless vfs_url

      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
      virtual_file = virtual_file.get_child( child_path ) if child_path
      raise Errno::ENOENT.new(filename) unless virtual_file.exists?

      Time.at( virtual_file.getLastModified() / 1000 )
    end

    def size(filename)
      return size_without_vfs(filename) if ( File.exist_without_vfs?( filename ) )


      vfs_url, child_path = TorqueBox::VFS.resolve_within_archive(filename)
      raise Errno::ENOENT.new(filename) unless vfs_url

      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )

      raise Errno::ENOENT.new(filename) unless virtual_file.exists

      virtual_file = virtual_file.get_child( child_path ) if child_path
      raise Errno::ENOENT.new(filename) unless virtual_file.exists

      virtual_file.size
    end

    def size?(filename)
      return size_without_vfs?(filename) if ( File.exist_without_vfs?( filename ) )

      vfs_url, child_path = TorqueBox::VFS.resolve_within_archive(filename)
      return nil unless vfs_url

      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
      virtual_file = virtual_file.get_child( child_path ) if child_path

      return nil unless virtual_file.exists

      virtual_file.size
    end

    def stat(filename)
      name = name_without_vfs(filename)
      return stat_without_vfs(name) if ( File.exist_without_vfs?( name ) )

      vfs_url, child_path = TorqueBox::VFS.resolve_within_archive(filename)
      raise Errno::ENOENT.new(filename) unless vfs_url

      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
      virtual_file = virtual_file.get_child( child_path ) if child_path
      raise Errno::ENOENT.new(filename) unless virtual_file.exists?

      TorqueBox::VFS::File::Stat.new( virtual_file )
    end

    def lstat(filename)
      name = name_without_vfs(filename)
      return lstat_without_vfs(name) if ( File.exist_without_vfs?( name ) )
      stat(filename)
    end

    def rename(oldname, newname) 
      rename_without_vfs( name_without_vfs(oldname), name_without_vfs(newname) )
    end

    def exist?(filename)
      return true if exist_without_vfs?( filename )

      virtual_file = virtual_file(filename)
      !virtual_file.nil? && virtual_file.exists?
    end
    alias_method :exists?, :exist?
    
    def writable?(filename)
      stat = stat(filename)
      return stat.writable? if stat
      false
    rescue Errno::ENOENT
      false
    end

    def directory?(filename)
      return directory_without_vfs?( filename ) if exist_without_vfs?( filename )

      virtual_file = virtual_file(filename)
      !virtual_file.nil? && virtual_file.is_directory?
    end

    def file?(filename)
      return true if file_without_vfs?( filename )

      virtual_file = virtual_file(filename)
      !virtual_file.nil? && virtual_file.is_leaf?
    end

    def dirname(filename)
      dirname = dirname_without_vfs(name_without_vfs(filename))
      vfs_path?(filename) ? TorqueBox::VFS.resolve_path_url(dirname) : dirname
    end

    def join(*args)
      prefix = vfs_path?(args[0]) ? "vfs:" : ""
      args = args.map do |arg|
        if arg.is_a?(Array)
          raise ArgumentError if arg.include?(arg)
          join(*arg)
        else
          arg
        end
      end
      prefix + join_without_vfs(*args.map{|x|name_without_vfs(x)})
    end

    def new(*args, &block)
      fname = args.size > 0 ? args[0] : nil
      if ( Fixnum === fname )
        return new_without_vfs( *args, &block )
      end
      unless ( vfs_path?(fname) )
        return new_without_vfs( *args, &block )
      end
      if ( File.exist_without_vfs?( name_without_vfs(fname) ) )
        args[0] = name_without_vfs(fname)
        return new_without_vfs( *args, &block )
      end
      # File.new doesn't pass a block through to the opened file
      IO.vfs_open( *args )
    end

    def chmod(mode_int, *files)
      writable_operation(*files) do |filename| 
        chmod_without_vfs( mode_int, filename )
      end
    end

    def chown(owner, group, *files)
      writable_operation(*files) do |filename|
        chown_without_vfs( owner, group, filename )
      end
    end

    def utime(accesstime, modtime, *files)
      writable_operation(*files) do |filename|
        utime_without_vfs( accesstime, modtime, filename )
      end
    end
    
    def writable_operation(*files)
      files.each do |name|
        begin
          yield name_without_vfs(name)
        rescue Errno::ENOENT => e
          yield TorqueBox::VFS.writable_path_or_error( name, e )
        end
      end
      files.size
    end

    def name_without_vfs(filename)
      name = path_to_str(filename).gsub("\\", "/")
      if vfs_path?(name) 
        result = name[4..-1]
        result.size==0 ? "/" : result
      else
        name
      end
    end

    def path_to_str(path)
      # 1.9 - to_str became to_path for Pathnames
      return path.to_path if path.respond_to?(:to_path) 
      raise TypeError unless path.respond_to?(:to_str)
      path.to_str
    end
    
    def vfs_path?(path)
      path.to_s =~ /^vfs:/
    end

    def virtual_file(filename)
      TorqueBox::VFS.virtual_file(filename)
    end
  end

end
