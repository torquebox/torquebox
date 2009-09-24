
class File

  class << self

    def mtime(filename)
      file = org.jboss.virtual.VFS.root( filename )
      Time.at( file.last_modified )
    end

  end

end
