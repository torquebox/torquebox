
class Java::org.jboss.vfs::VFS
  def self.child(uri_str)
    uri = java.net.URI.new( uri_str )
    child = self.getChild( uri )
    child
  end
end
