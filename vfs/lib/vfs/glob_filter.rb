
module VFS
  class GlobFilter
    include org.jboss.virtual.VirtualFileFilter
  
    def initialize(glob)
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
      regexp_str = "^#{regexp_str}$"
      @regexp = Regexp.new( regexp_str )
    end
  
    def accepts(file)
      !!( file.path_name =~ @regexp )
    end
  end
end
