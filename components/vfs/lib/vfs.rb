
require 'java'

module VFS
end

require 'vfs/file'
require 'vfs/dir'
require 'vfs/glob_filter'
require 'vfs/ext/vfs'
require 'vfs/ext/io'
require 'vfs/ext/file'
require 'vfs/ext/dir'
require 'vfs/ext/kernel'


module ::VFS
  def self.resolve_within_archive(path)
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
    prefix = "vfs:"
    prefix += "#{::Dir.pwd}/" unless ( path =~ /^\// )
    puts "prefix #{prefix}"
    base = "#{prefix}#{path}"
    puts "resolve_path_url(#{path}) ==> #{base}"
    base
  end

end
