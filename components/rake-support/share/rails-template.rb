
if Rails::VERSION::STRING.start_with?( "2" )
  # Rails 2.x.x
  gem "activerecord-jdbc-adapter", :lib => "jdbc_adapter"
  gem "org.torquebox.rake-support", :lib => 'torquebox-rails'
else
  # Rails 3.x.x (or higher)
  run %q[sed -i '' -e 's/^\(gem.*sqlite3\)/# \1/' Gemfile]
  gem "activerecord-jdbc-adapter", "0.9.7", :require => "jdbc_adapter"
  gem "jdbc-sqlite3"
  gem "org.torquebox.rake-support", :require => 'torquebox-rails'
end

# Create app/tasks and app/jobs, just for fun
inside('app') {
  FileUtils.mkdir %w( tasks jobs )
}

# We need the app to find the rake tasks
rakefile( 'torquebox.rake' ) do
  <<-TASK
require 'torquebox/tasks'

# Patch db:load_config to make sure activerecord-jdbc-adapter gets loaded
namespace :db do
  task :load_config => :rails_env do
    require 'active_record'
    require 'activerecord-jdbc-adapter'
    ActiveRecord::Base.configurations = Rails::Configuration.new.database_configuration
  end
end

  TASK
end
