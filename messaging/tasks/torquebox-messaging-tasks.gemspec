
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "torquebox-messaging-tasks"
    s.version   =   "#{PROJECT_VERSION}"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "TorqueBox Messaging Tasks"
    s.files     =   [
      'lib/torquebox-messaging-tasks.rb',
      'lib/torquebox/messaging/tasks.rb',
    ].flatten
    s.require_paths  =   [ 'lib' ]
    s.has_rdoc  =   true
    s.add_dependency 'torquebox-messaging-client', s.version
end

