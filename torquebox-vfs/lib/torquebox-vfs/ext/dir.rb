
class Dir

  class << self
    def glob(pattern,flags=nil)
      puts "jack into glob() [#{pattern}]"
      first_special = ( pattern =~ /[\*\?\[\{]/ )
      base    = pattern[0, first_special]
      matcher = pattern[first_special..-1]
      puts "base #{base}"
      puts "matcher #{matcher}"
      root = org.jboss.virtual.VFS.root( base )
      puts "ROOT #{root}"
      root.children_recursively( GlobFilter.new( matcher ) ).collect{|e| "#{base}#{e.path_name}"}
    end
  end

end


class GlobFilter
  include org.jboss.virtual.VirtualFileFilter

  def initialize(glob)
    glob_segments = glob.split( '/' )
    regexp_segments = []

    glob_segments.each do |gs|
      if ( gs == '**' )
        regexp_segments << '.*'
      elsif ( gs =~ /\*/ )
        regexp_segments << gs.gsub( /\*/, '[^\/]*')
      else
        regexp_segments << gs
      end
    end
    
    regexp_str = regexp_segments.join( '/' )
    regexp_str = "^#{regexp_str}$"
    puts "using regexp [#{regexp_str}]"
    @regexp = Regexp.new( regexp_str )
  end

  def accepts(file)
    puts "testing #{file.path_name}"
    !!( file.path_name =~ @regexp )
  end

  def compare_segments(path)
    path_segments = path.split( '/' )
  end

end
