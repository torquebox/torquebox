
class Java::OrgJbossVirtual::VFS

  def self.root(uri_str)
    uri = java.net.URI.new( uri_str )
    self.getRoot( uri )
  end

end

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

