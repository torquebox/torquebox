
require 'java'

require 'vfs/ext/vfs'
require 'vfs/dir'
require 'vfs/glob_filter'
require 'vfs/ext/io'
require 'vfs/ext/file'
require 'vfs/ext/dir'


module VFS
  def self.first_existing(path)
    cur = path
    while ( cur != '.' && cur != '/' )
      if ( File.exist_without_vfs?( cur ) )
        if ( cur[-1,1] == '/' )
          cur = cur[0..-2]
        end
        return cur
      end
      cur = File.dirname( cur )
    end
    nil
  end
end

