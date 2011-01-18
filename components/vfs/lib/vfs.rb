
require 'java'

module VFS
end

require 'vfs/file'
require 'vfs/dir'
require 'vfs/glob_filter'
require 'vfs/ext/vfs'
require 'vfs/ext/io'
require 'vfs/ext/tempfile'      # must be required before file.rb!
require 'vfs/ext/file'
require 'vfs/ext/file_test'
require 'vfs/ext/dir'
require 'vfs/ext/pathname'
require 'vfs/ext/kernel'
require 'vfs/ext/jdbc'


module ::VFS
  def self.resolve_within_archive(path)
    path = path.to_s
    return path if ( path =~ %r(^vfs:) )
    cur = path
    while ( cur != '.' && cur != '/' )
      if ( ::File.exist_without_vfs?( cur ) )

        child_path = path[cur.length..-1]

        if ( cur[-1,1] == '/' )
          cur = cur[0..-2]
        end
        return ::VFS.resolve_path_url( cur ), child_path
      end
      cur = ::File.dirname( cur ) + '/'
    end
    nil
  end

  def self.resolve_path_url(path)
    prefix = case
             when path =~ /^\//            # unix absolute
               "vfs:"
             when path =~ /^[[:alpha:]]:/  # windows absolute
               "vfs:/"
             else
               "#{resolve_path_url( ::Dir.pwd )}/"
             end
    "#{prefix}#{path}"
  end

  def self.virtual_file(filename)
    vfs_url, child_path = VFS.resolve_within_archive( filename )
    return nil unless vfs_url

    begin
      virtual_file = Java::org.jboss.vfs.VFS.child( vfs_url )
      virtual_file = virtual_file.get_child( child_path ) if child_path
      virtual_file
    rescue Java::JavaIo::IOException => e
      nil
    end
  end

  def self.writable_path_or_error(path, e)
    virtual_file = VFS.virtual_file( path )
    raise e if virtual_file.nil?
    mount = Java::org.jboss.vfs::VFS.get_mount(virtual_file)
    # TODO: Replace with a better error stating the issue, which is
    # the user is trying to write to a filesystem inside an archive
    # that is mounted as readonly
    #
    # HACK: For some reason mount.file_system doesn't work inside TB
    # but does in tests
    # raise e if mount.file_system.read_only?
    virtual_file.physical_file.path
  end

end
