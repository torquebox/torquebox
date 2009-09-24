
class IO

  class << self
    alias_method :open_before_vfs, :open
    alias_method :read_before_vfs, :read

    def open(fd,mode_str='r')
      file = org.jboss.virtual.VFS.root( fd )
      stream = file.openStream()
      io = stream.to_io 
      if ( block_given? )
        begin
          yield( io )
        ensure
          io.close()
        end
      end
      io
     
      #open_before_vfs(*args)
    end

    def read(name,length=nil,offset=nil)
      puts "jack into read"
      file = org.jboss.virtual.VFS.root( name )
      stream = file.openStream()
      io = stream.to_io 
      begin
        s = io.read
      ensure
        io.close()
      end
      s
      #read_before_vfs(*args)
    end
  end

end
