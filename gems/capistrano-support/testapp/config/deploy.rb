require 'torquebox-capistrano-support'
require 'bundler/capistrano'

set :application, "testapp"
set :repository,  "."
set :user,        ENV['user']
set :deploy_to,   "/home/#{ENV['LOGNAME']}/apps/testapp"
set :deploy_via,  :copy
set :use_sudo,    false

set :torquebox_home,    "/home/#{ENV['LOGNAME']}/torquebox-current"

set :jboss_control_style, :binscripts

#set :app_environment, {
  #'FOO'=>'bar'
#}
set :rails_env, "production"

#set :app_host, 'taco.com'
#set :app_context, '/myapp'

#set :jboss_init_script, "#{jboss_home}/bin/jboss_init_redhat.sh"
#set :jboss_user,        :self

set :scm, :none
# Or: `accurev`, `bzr`, `cvs`, `darcs`, `git`, `mercurial`, `perforce`, `subversion` or `none`

role :web, "captest.local"                          # Your HTTP server, Apache/etc
role :app, "captest.local"                          # This may be the same as your `Web` server
role :db,  "captest.local", :primary => true        # This is where Rails migrations will run

#task :what do
  #puts exists?(:app_host)
  #puts variables[:app_host].inspect
  #puts exists?(:app_context)
  #puts variables[:app_context].inspect
#end
