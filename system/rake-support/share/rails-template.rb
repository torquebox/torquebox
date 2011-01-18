
if ( Rails::VERSION::MAJOR == 2 )
  gem "activerecord-jdbc-adapter", :lib => "jdbc_adapter"
  gem "org.torquebox.rake-support", :lib => 'torquebox-rails'
else
  text = File.read 'Gemfile'
  File.open('Gemfile', 'w') {|f| f << text.gsub(/^(gem 'sqlite3)/, '# \1') }
  gem "activerecord-jdbc-adapter", "0.9.7", :require => "jdbc_adapter"
  gem "jdbc-sqlite3"
  gem "jruby-openssl"
  gem "org.torquebox.rake-support", :require => 'torquebox-rails'
end

if ( Rails::VERSION::MAJOR == 2 )
  initializer("session_store.rb") do
    <<-INIT
# Configure the TorqueBox Servlet-based session store.
# Provides for server-based, in-memory, cluster-compatible sessions.
( ActionController::Base.session_store = TorqueBox::Session::ServletStore ) if defined?(TorqueBox::Session::ServletStore)
    INIT
  end
else
  remove_file( 'config/initializers/session_store.rb' )
  initializer("session_store.rb") do
    <<-INIT
# Configure the TorqueBox Servlet-based session store.
# Provides for server-based, in-memory, cluster-compatible sessions.
#{app_const}.config.session_store TorqueBox::Session::ServletStore if defined?(TorqueBox::Session::ServletStore)
    INIT
  end
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
    ActiveRecord::Base.configurations = Rails::Application.config.database_configuration
  end
end

  TASK
end
