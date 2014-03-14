# -*- encoding: utf-8 -*-
# stub: activerecord-jdbc-adapter 1.3.4 ruby lib

Gem::Specification.new do |s|
  s.name = "activerecord-jdbc-adapter"
  s.version = "1.3.4"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Nick Sieger, Ola Bini and JRuby contributors"]
  s.date = "2013-12-12"
  s.description = "AR-JDBC is a database adapter for Rails' ActiveRecord component designed to be used with JRuby built upon Java's JDBC API for database access. Provides (ActiveRecord) built-in adapters: MySQL, PostgreSQL and SQLite3 as well as adapters for popular databases such as Oracle, SQLServer, DB2, FireBird and even Java (embed) databases: Derby, HSQLDB and H2. It allows to connect to virtually any JDBC-compliant database with your JRuby on Rails application."
  s.email = ["nick@nicksieger.com", "ola.bini@gmail.com"]
  s.homepage = "https://github.com/jruby/activerecord-jdbc-adapter"
  s.licenses = ["BSD"]
  s.rdoc_options = ["--main", "README.md", "-SHN", "-f", "darkfish"]
  s.require_paths = ["lib"]
  s.rubyforge_project = "jruby-extras"
  s.rubygems_version = "2.1.9"
  s.summary = "JDBC adapter for ActiveRecord, for use within JRuby on Rails."

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<activerecord>, [">= 2.2"])
    else
      s.add_dependency(%q<activerecord>, [">= 2.2"])
    end
  else
    s.add_dependency(%q<activerecord>, [">= 2.2"])
  end
end
