# -*- encoding: utf-8 -*-
# stub: thor 0.18.1 ruby lib

Gem::Specification.new do |s|
  s.name = "thor"
  s.version = "0.18.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 1.3.6") if s.respond_to? :required_rubygems_version=
  s.authors = ["Yehuda Katz", "Jos\u{e9} Valim"]
  s.date = "2013-03-30"
  s.description = "A scripting framework that replaces rake, sake and rubigen"
  s.email = "ruby-thor@googlegroups.com"
  s.executables = ["thor"]
  s.files = ["bin/thor"]
  s.homepage = "http://whatisthor.com/"
  s.licenses = ["MIT"]
  s.require_paths = ["lib"]
  s.rubygems_version = "2.1.9"
  s.summary = "A scripting framework that replaces rake, sake and rubigen"

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<bundler>, ["~> 1.0"])
    else
      s.add_dependency(%q<bundler>, ["~> 1.0"])
    end
  else
    s.add_dependency(%q<bundler>, ["~> 1.0"])
  end
end
