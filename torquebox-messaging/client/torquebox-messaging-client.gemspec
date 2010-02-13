
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "torquebox-messaging-client"
    s.version   =   "#{PROJECT_VERSION}"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "TorqueBox Messaging Client"
    s.files     =   [
      'lib/torquebox-messaging-client.rb',
      'lib/torquebox/messaging/client.rb',
      Dir['java-lib/**/*.jar'],
    ].flatten
    s.require_paths  =   [ 'lib', 'java-lib' ]
    s.has_rdoc  =   true
end

