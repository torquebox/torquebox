RAILS_2 = Rails::VERSION::MAJOR == 2
RAILS_3_0 = Rails::VERSION::MAJOR == 3 && Rails::VERSION::MINOR == 0
RAILS_3_1 = Rails::VERSION::MAJOR == 3 && Rails::VERSION::MINOR == 1

if RAILS_2
  gem "activerecord-jdbc-adapter", :lib => "arjdbc"
elsif RAILS_3_0 
  text = File.read 'Gemfile'
  File.open('Gemfile', 'w') {|f| f << text.gsub(/^(gem 'sqlite3)/, '# \1') }
  gem "activerecord-jdbc-adapter", :require => "arjdbc"
  gem "jdbc-sqlite3"
  gem "jruby-openssl"
else
  # rails 3.1+ properly detects jruby and does the right thing
end

if RAILS_2
  gem 'torquebox', :version => "${env.BUILD_NUMBER}"
else
  gem 'torquebox', "${env.BUILD_NUMBER}"
end


# Write a dummy torquebox.yml file
if File.exists? 'config/torquebox.yml'
  puts "TorqueBox configuration file already exists."
else
  File.open('config/torquebox.yml', 'w') do |f|
    f << <<-TORQUEBOX_CONFIG
---
# This is the TorqueBox configuration file. Refer to the TorqueBox
# documentation at http://torquebox.org/documentation/current/ 
# for all configuration options.
web:
  context: "/"
    TORQUEBOX_CONFIG
  end
end

if RAILS_2 
  initializer("session_store.rb") do
    <<-INIT
# Configure the TorqueBox Servlet-based session store.
# Provides for server-based, in-memory, cluster-compatible sessions.
if ENV['TORQUEBOX_APP_NAME']
  ActionController::Base.session_store = :torquebox_store
else
  ActionController::Base.session = { :session_key => '_CHANGEME_session', :secret => 'CHANGEME_107f23805ff8eed10736e03d1d8ab229706afedfa8d7918be604dc291d6772c56cdf94d2b7a3d656d1ebc8ed83d91c2042445670208f07df38acb4ebe93bbef7' }
  ActionController::Base.session_store = :cookie_store
end  
    INIT
  end
else
  remove_file( 'config/initializers/session_store.rb' )
  initializer("session_store.rb") do
    <<-INIT
# Configure the TorqueBox Servlet-based session store.
# Provides for server-based, in-memory, cluster-compatible sessions
if ENV['TORQUEBOX_APP_NAME']
  #{app_const}.config.session_store :torquebox_store
else
  #{app_const}.config.session_store :cookie_store, :key => '_CHANGEME_session'
end  
    INIT
  end
end

environment do
  <<-ENVIRONMENT
  # Use TorqueBox::Infinispan::Cache for the Rails cache store
  if defined? TorqueBox::Infinispan::Cache
    config.cache_store = :torquebox_store
  end
  ENVIRONMENT
end

initializer("active_record_backgroundable.rb") do
  <<-INIT
# Enable backgroundable methods for ActiveRecord classes. Provides:
# class AModel < ActiveRecord::Base
#   always_background :a_method
# end
# 
# a_model_instance.background.another_method
if defined?(TorqueBox::Messaging::Backgroundable) && defined?(ActiveRecord::Base)
  ActiveRecord::Base.send(:include, TorqueBox::Messaging::Backgroundable)
end
  INIT
end

# Create directories for jobs, services, and processors just for fun
inside('app') {
  %w(jobs services processors).each { |dir| FileUtils.mkdir(dir) unless File.exists?(dir) }
}

app_constant = RAILS_2 ? 'Rails::Application' : app_const

rake_task = <<TASK
begin
  require 'torquebox-rake-support'
rescue LoadError => ex
  puts "Failed to load the TorqueBox rake gem (torquebox-rake-support). Make sure it is available in your environment."
end
TASK

if RAILS_2 || RAILS_3_0 
  rake_task << <<TASK

# Patch db:load_config to make sure activerecord-jdbc-adapter gets loaded
namespace :db do
  task :load_config => :rails_env do
    require 'active_record'
    require 'activerecord-jdbc-adapter'
    ActiveRecord::Base.configurations = #{app_constant}.config.database_configuration
  end
end
TASK
end

# We need the app to find the rake tasks
rakefile( 'torquebox.rake', rake_task )

