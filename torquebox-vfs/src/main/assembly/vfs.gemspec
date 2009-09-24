
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "vfs"
    s.version   =   "#{PROJECT_VERSION}"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "JBoss VFS Support."
    s.files     =   [
      Dir['lib/**/*.rb'],
    ].flatten
    s.require_path  =   "lib"
    s.has_rdoc      =   true
end

