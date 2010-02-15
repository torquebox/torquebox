
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "torquebox-messaging-runtime"
    s.version   =   "#{PROJECT_VERSION}"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "TorqueBox Messaging Runtime"
    s.files     =   [
      Dir['lib/**/*.rb'],
      Dir['java-lib/**/*.jar'],
    ].flatten
    s.require_paths  =   [ 'lib', 'java-lib' ]
    s.has_rdoc  =   true
end

