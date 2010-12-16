require 'vfs/glob_translator'


module VFS
  class GlobFilter
    include Java::org.jboss.vfs.VirtualFileFilter
  
    def initialize(child_path, glob)
      regexp_str = GlobTranslator.translate( glob )
      if ( child_path && child_path != '' )
        if ( child_path[-1,1] == '/' )
          regexp_str = "^#{child_path}#{regexp_str}$"
        else
          regexp_str = "^#{child_path}/#{regexp_str}$"
        end
      else
        regexp_str = "^#{regexp_str}$"
      end
      @regexp = Regexp.new( regexp_str ) 
    end
  
    def accepts(file)
      !!( file.path_name =~ @regexp )
    end
  end
end

