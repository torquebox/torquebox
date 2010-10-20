
# Comment out the MRI sqlite3 gem requiring native extensions
run %q[sed -i '' -e 's/^\(gem.*sqlite3\)/# \1/' Gemfile]

# Create app/tasks and app/jobs, just for fun
inside('app') {
  FileUtils.mkdir %w( tasks jobs )
}

# We need the jdbc adapter
gem "activerecord-jdbc-adapter", :require=>'arjdbc'
# We need the torquebox rake tasks 
gem "org.torquebox.rake-support"

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
