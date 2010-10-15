
class Java::org.jboss.vfs::VFS
  def self.child(uri_str)
    uri = java.net.URI.new( uri_str.gsub('#', '%23').gsub(' ', '%20') )
    child = self.getChild( uri )
    child
  end
end
