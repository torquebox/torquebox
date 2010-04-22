
require 'rubygems'

Gem::Specification.new do |s|
    s.platform  =   Gem::Platform::RUBY
    s.name      =   "torquebox-rails"
    s.version   =   "#{PROJECT_VERSION}"
    s.author    =   "The TorqueBox Project"
    s.email     =   "info@torquebox.org"
    s.summary   =   "TorqueBox Rails Support."
    s.files     =   [
      Dir['lib/**/*.rb'],
      'init.rb'
    ].flatten
    s.require_path  =   "lib"
    s.has_rdoc  =   true
end

