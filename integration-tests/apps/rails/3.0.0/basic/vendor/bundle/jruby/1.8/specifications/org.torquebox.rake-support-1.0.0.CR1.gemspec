# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{org.torquebox.rake-support}
  s.version = "1.0.0.CR1"

  s.required_rubygems_version = Gem::Requirement.new("> 1.3.1") if s.respond_to? :required_rubygems_version=
  s.date = %q{2011-01-15}
  s.description = %q{}
  s.files = ["licenses/lgpl-2.1.txt", "lib/torquebox-rails.rb", "lib/torquebox/tasks.rb", "lib/torquebox/tasks/archive.rb", "lib/torquebox/tasks/deployment.rb", "lib/torquebox/tasks/rake_utils.rb", "lib/torquebox/tasks/server.rb", "generators/torquebox_queue_generator.rb", "generators/USAGE", "generators/templates/queue.rb"]
  s.homepage = %q{http://www.torquebox.org/rake-support/}
  s.licenses = ["lgpl"]
  s.require_paths = ["lib"]
  s.rubygems_version = %q{1.4.2}
  s.summary = %q{TorqueBox Rake Support}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
    else
    end
  else
  end
end
