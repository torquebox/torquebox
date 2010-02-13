
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "torquebox-messaging-container"
    s.version   =   "#{PROJECT_VERSION}"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "TorqueBox Messaging Container"
    s.files     =   [
      'lib/torquebox-messaging-container.rb',
      'lib/torquebox/messaging/container.rb',
      'lib/torquebox/messaging/config.rb',
      Dir['java-lib/**/*.jar'],
    ].flatten
    s.require_paths  =   [ 'lib', 'java-lib' ]
    s.has_rdoc  =   true
end

