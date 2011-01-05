require 'vfs/glob_translator'


module VFS
  class DebugFilter
    include Java::org.jboss.vfs.VirtualFileFilter
  
    def initialize()
    end
  
    def accepts(file)
      puts "visit #{file}"
      true
    end

  end
end

