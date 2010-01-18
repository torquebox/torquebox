

module VFS
  class Dir
    attr_reader :path
    attr_reader :pos
    alias_method :tell, :pos

    def initialize(path)
      @path         = path
      begin
        @virtual_file = org.jboss.virtual.VFS.root( path )
      rescue Java::JavaLang::NullPointerException 
        raise Errno::ENOENT.new
      end
      @pos          = 0
      @closed       = false
    end

    def close
      @closed = true
    end

    def each
      @virtual_file.children.each do |child|
        yield child.name
      end
    end

    def rewind
      @pos = 0
    end

    def read
      children = @virtual_file.children
      return nil unless ( @pos < children.size )
      child = children[@pos]
      @pos += 1
      child.name
    end
    
    def seek(i)
      @pos = i
      self
    end

    def pos=(i)
      @pos = i
    end

  end

end 

