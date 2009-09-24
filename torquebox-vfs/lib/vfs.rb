
require 'java'

require 'vfs/ext/vfs'
require 'vfs/dir'
require 'vfs/ext/dir'
require 'vfs/glob_filter'
require 'vfs/ext/io'


module VFS
  def self.first_existing(path)
    cur = path
    while ( cur != '.' && cur != '/' )
      if ( File.exist?( cur ) )
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

