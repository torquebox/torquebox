
class Java::org.jboss.vfs::VFS
  def self.child(uri_str)
    uri = java.net.URI.new( uri_str )
    self.getChild( uri )
  end
end
