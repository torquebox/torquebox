
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "${project.groupId}.${project.artifactId}"
    s.version   =   "1.0.0.19"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "TorqueBox VFS Support."
    s.files     =   [
      Dir['lib/**/*.rb'],
    ].flatten
    s.require_path  =   "lib"
    s.has_rdoc      =   true
end

