
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "torquebox-messaging"
    s.version   =   "#{PROJECT_VERSION}"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "TorqueBox Messaging"
    s.files     =   [
      'init.rb',
      Dir['lib/**/*.rb'],
      Dir['lib/**/*.jar'],
    ].flatten
    s.require_path  =   "lib"
    s.has_rdoc  =   true
end

