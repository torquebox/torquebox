
begin
  require 'torquebox-rake-support'
rescue LoadError => ex
  puts "Failed to load the TorqueBox rake gem (torquebox-rake-support). Make sure it is available in your environment."
end

# Patch db:load_config to make sure activerecord-jdbc-adapter gets loaded
namespace :db do
  task :load_config => :rails_env do
    require 'active_record'
    require 'activerecord-jdbc-adapter'
    ActiveRecord::Base.configurations = Rails::Application.config.database_configuration
  end
end

