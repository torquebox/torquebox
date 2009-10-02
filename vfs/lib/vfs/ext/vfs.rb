
class Java::OrgJbossVirtual::VFS
  def self.root(uri_str)
    uri = java.net.URI.new( uri_str )
    self.getRoot( uri )
  end
end
