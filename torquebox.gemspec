require "#{File.dirname(__FILE__)}/core/lib/torquebox/version"

Gem::Specification.new do |s|
  s.name     = 'torquebox'
  s.version  = TorqueBox::VERSION
  s.platform = 'java'
  s.summary  = 'TorqueBox Next Generation'
  s.author   = 'The TorqueBox Team'
  s.email    = 'torquebox-dev@torquebox.org'
  s.homepage = 'http://torquebox.org/4x'

  s.required_ruby_version = '>= 1.9.3'
  s.license = 'Apache-2.0'

  s.require_paths = ["lib"]
  s.files         = Dir['CHANGELOG.md', 'README.md', 'LICENSE',
                        'bin/**/*', 'lib/**/*']

  s.add_dependency 'torquebox-core', TorqueBox::VERSION
  s.add_dependency 'torquebox-caching', TorqueBox::VERSION
  s.add_dependency 'torquebox-messaging', TorqueBox::VERSION
  s.add_dependency 'torquebox-scheduling', TorqueBox::VERSION
  s.add_dependency 'torquebox-web', TorqueBox::VERSION

  s.add_development_dependency('jbundler', '~> 0.5.4')
  s.add_development_dependency('rake')
  s.add_development_dependency('rake-compiler')
  s.add_development_dependency('rspec', '~> 3.5.0')
  s.add_development_dependency('torquespec', '~> 0.6')
  s.add_development_dependency('mime-types', '~> 2.6.2')
  # Pin a broken transitive dep of poltergeist
  s.add_development_dependency('rainbow', '2.1.0')
  s.add_development_dependency('poltergeist', '~> 1.12.0')
  s.add_development_dependency('websocket-client-simple', '~> 0.2.2')
  s.add_development_dependency('activesupport', '~> 4.1')
  s.add_development_dependency('edn', '1.0.3')
  s.add_development_dependency('yard', '~> 0.8.7.4')
  s.add_development_dependency('yard-doctest', '~> 0.1.2')
  s.add_development_dependency('kramdown', '~> 1.4.1')
  s.add_development_dependency('builder', '~> 3.2.2')
  s.add_development_dependency('rubocop', '~> 0.46.0')
end
