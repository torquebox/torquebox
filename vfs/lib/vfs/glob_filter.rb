

# puts "Defining VFS::GlobFilter #{__FILE__}:#{__LINE__}"
# puts "VFF=#{Java::org.jboss.virtual.VirtualFileFilter} #{__FILE__}:#{__LINE__}"
module VFS
  class GlobFilter
    include Java::org.jboss.virtual.VirtualFileFilter
  
    def initialize(child_path, glob)
      @child_path = child_path
      glob_segments = glob.split( '/' )
      regexp_segments = []
  
      glob_segments.each_with_index do |gs,i|
        if ( gs == '**' )
          regexp_segments << '(.*)'
        else
          gs.gsub!( /\*/, '[^\/]*')
          gs.gsub!( /\?/, '.')
          gs.gsub!( /\{[^\}]+\}/ ) do |m|
            options = m[1..-2].split(',')
            options = options.collect{|e| "(#{e})"}
            "(#{options.join('|')})"
          end
          if ( i < (glob_segments.size()-1))
            gs = "#{gs}/"
          end
          regexp_segments << gs
        end
      end
      
      regexp_str = regexp_segments.join
      ##puts "regexp_str(1) [#{regexp_str}]"
      if ( @child_path && @child_path != '' )
        regexp_str = ::File.join( "^#{@child_path}", "#{regexp_str}$" )
      else
        regexp_str = "^#{regexp_str}$"
      end
      #puts "regexp_str(2) [#{regexp_str}]"
      @regexp = Regexp.new( regexp_str )
    end
  
    def accepts(file)
      #puts "accepts(#{file.path_name}) vs #{@regexp}"
      acceptable = ( !!( file.path_name =~ @regexp ) )
      #puts "   -> #{acceptable}"
      !!( file.path_name =~ @regexp )
    end
  end
end

# puts "Defined VFS::GlobFilter #{__FILE__}:#{__LINE__}"
