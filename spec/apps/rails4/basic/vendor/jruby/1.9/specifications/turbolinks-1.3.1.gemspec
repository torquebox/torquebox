# -*- encoding: utf-8 -*-
# stub: turbolinks 1.3.1 ruby lib

Gem::Specification.new do |s|
  s.name = "turbolinks"
  s.version = "1.3.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["David Heinemeier Hansson"]
  s.date = "2013-11-14"
  s.email = "david@loudthinking.com"
  s.licenses = ["MIT"]
  s.require_paths = ["lib"]
  s.rubygems_version = "2.1.9"
  s.summary = "Turbolinks makes following links in your web application faster (use with Rails Asset Pipeline)"

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<coffee-rails>, [">= 0"])
    else
      s.add_dependency(%q<coffee-rails>, [">= 0"])
    end
  else
    s.add_dependency(%q<coffee-rails>, [">= 0"])
  end
end
