# -*- encoding: utf-8 -*-
$:.push(File.expand_path('../lib', __FILE__))
require 'torqbox/version'

Gem::Specification.new do |s|
  s.name     = 'torqbox'
  s.version  = TorqBox::VERSION
  s.platform = 'java'
  s.summary  = 'TorqueBox Next Generation'
  s.author   = 'The TorqueBox Team'
  s.email    = 'torquebox-dev@torquebox.org'
  s.homepage = 'http://torquebox.org/torqbox'

  s.required_ruby_version = '>= 1.9.3'
  s.license = 'LGPL3'

  s.require_paths = ["lib"]
  s.bindir        = 'bin'
  s.executables   = ['torqbox']
  s.files         = Dir['CHANGELOG.md', 'README.md', 'LICENSE',
                        'bin/**/*', 'lib/**/*']

  s.add_dependency 'rack', '>= 1.4.0', '< 2.0'

  s.add_development_dependency('rake')
  s.add_development_dependency('rspec', '~> 2.14')
end
