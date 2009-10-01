
require 'pp'

class Java::OrgJbossVirtual::VFS
  def self.root(uri_str)
    #puts "VFS.root(#{uri_str})"
    #pp caller
    uri = java.net.URI.new( uri_str )
    self.getRoot( uri )
  end
end
