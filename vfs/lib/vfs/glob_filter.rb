
module VFS
  class GlobFilter
    include org.jboss.virtual.VirtualFileFilter
  
    def initialize(child_path, glob)
      @child_path = child_path
      glob_segments = glob.split( '/' )
      regexp_segments = []
  
      glob_segments.each do |gs|
        if ( gs == '**' )
          regexp_segments << '.*'
        else
          gs.gsub!( /\*/, '[^\/]*')
          gs.gsub!( /\?/, '.')
          gs.gsub!( /\{[^\}]+\}/ ) do |m|
            options = m[1..-2].split(',')
            options = options.collect{|e| "(#{e})"}
            "(#{options.join('|')})"
          end
          regexp_segments << gs
        end
      end
      
      regexp_str = regexp_segments.join( '/' )
      if ( @child_path && @child_path != '' )
        regexp_str = "^#{@child_path}/#{regexp_str}$"
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
