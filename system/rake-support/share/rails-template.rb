
if ( Rails::VERSION::MAJOR == 2 )
  gem "activerecord-jdbc-adapter", :lib => "jdbc_adapter"
else
  text = File.read 'Gemfile'
  File.open('Gemfile', 'w') {|f| f << text.gsub(/^(gem 'sqlite3)/, '# \1') }
  gem "activerecord-jdbc-adapter", "0.9.7", :require => "jdbc_adapter"
  gem "jdbc-sqlite3"
  gem "jruby-openssl"
end

# gems defs common to v2 and v3
gem "org.torquebox.rake-support"
gem 'org.torquebox.torquebox-messaging-client'


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
# Provides for server-based, in-memory, cluster-compatible sessions
#{app_const}.config.session_store TorqueBox::Session::ServletStore if defined?(TorqueBox::Session::ServletStore)
    INIT
  end
end

initializer("active_record_handle_async.rb") do
  <<-INIT
# Enable embedded tasks for ActiveRecord classes. Provides:
# class AModel < ActiveRecord::Base
#   always_background :a_method
# end
# 
# a_model_instance.background.another_method
if defined?(TorqueBox::Messaging) && defined?(ActiveRecord::Base)
  require 'torquebox/messaging/embedded_tasks'
  ActiveRecord::Base.send(:include, TorqueBox::Messaging::EmbeddedTasks )
end
  INIT
end

# Create app/tasks and app/jobs, just for fun
inside('app') {
  FileUtils.mkdir %w( tasks jobs )
}

# We need the app to find the rake tasks
rakefile( 'torquebox.rake' ) do
  <<-TASK

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
