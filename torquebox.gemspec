# -*- encoding: utf-8 -*-
require "#{File.dirname(__FILE__)}/core/lib/torquebox/version"

Gem::Specification.new do |s|
  s.name     = 'torquebox'
  s.version  = TorqueBox::VERSION
  s.platform = 'java'
  s.summary  = 'TorqueBox Next Generation'
  s.author   = 'The TorqueBox Team'
  s.email    = 'torquebox-dev@torquebox.org'
  s.homepage = 'http://torquebox.org/torqbox'

  s.required_ruby_version = '>= 1.9.3'
  s.license = 'LGPL3'

  s.require_paths = ["lib"]
  s.files         = Dir['CHANGELOG.md', 'README.md', 'LICENSE',
                        'bin/**/*', 'lib/**/*']

  s.add_dependency 'torquebox-core', TorqueBox::VERSION
  s.add_dependency 'torquebox-scheduling', TorqueBox::VERSION
  s.add_dependency 'torquebox-web', TorqueBox::VERSION

  s.add_development_dependency('jbundler')
  s.add_development_dependency('rake')
  s.add_development_dependency('rake-compiler')
  s.add_development_dependency('rspec', '~> 2.14')
  s.add_development_dependency('poltergeist', '~> 1.4.1')
  s.add_development_dependency('activesupport', '~> 4.1')
  s.add_development_dependency('yard', '~> 0.8.7.4')
end
