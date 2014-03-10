# -*- encoding: utf-8 -*-
# stub: jquery-rails 3.0.4 ruby lib

Gem::Specification.new do |s|
  s.name = "jquery-rails"
  s.version = "3.0.4"

  s.required_rubygems_version = Gem::Requirement.new(">= 1.3.6") if s.respond_to? :required_rubygems_version=
  s.authors = ["Andr\u{e9} Arko"]
  s.date = "2013-07-11"
  s.description = "This gem provides jQuery and the jQuery-ujs driver for your Rails 3 application."
  s.email = ["andre@arko.net"]
  s.homepage = "http://rubygems.org/gems/jquery-rails"
  s.licenses = ["MIT"]
  s.require_paths = ["lib"]
  s.rubyforge_project = "jquery-rails"
  s.rubygems_version = "2.1.9"
  s.summary = "Use jQuery with Rails 3"

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<railties>, ["< 5.0", ">= 3.0"])
      s.add_runtime_dependency(%q<thor>, ["< 2.0", ">= 0.14"])
    else
      s.add_dependency(%q<railties>, ["< 5.0", ">= 3.0"])
      s.add_dependency(%q<thor>, ["< 2.0", ">= 0.14"])
    end
  else
    s.add_dependency(%q<railties>, ["< 5.0", ">= 3.0"])
    s.add_dependency(%q<thor>, ["< 2.0", ">= 0.14"])
  end
end
