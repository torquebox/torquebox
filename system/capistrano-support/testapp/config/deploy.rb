require 'torquebox-capistrano-support'
require 'bundler/capistrano'

set :application, "testapp"
set :repository,  "."
set :user,        "bob"
set :deploy_to,   "/home/bob/apps/testapp"
set :deploy_via,  :copy
set :use_sudo,    false

set :torquebox_home,    '/home/bob/torquebox-current'
set :jboss_config,      :default

set :jboss_control_style, :binscripts

#set :jboss_init_script, "#{jboss_home}/bin/jboss_init_redhat.sh"
#set :jboss_user,        :self

set :scm, :none
# Or: `accurev`, `bzr`, `cvs`, `darcs`, `git`, `mercurial`, `perforce`, `subversion` or `none`

role :web, "captest.local"                          # Your HTTP server, Apache/etc
role :app, "captest.local"                          # This may be the same as your `Web` server
role :db,  "captest.local", :primary => true        # This is where Rails migrations will run



# If you are using Passenger mod_rails uncomment this:
# if you're still using the script/reapear helper you will need
# these http://github.com/rails/irs_process_scripts

# namespace :deploy do
#   task :start do ; end
#   task :stop do ; end
#   task :restart, :roles => :app, :except => { :no_release => true } do
#     run "#{try_sudo} touch #{File.join(current_path,'tmp','restart.txt')}"
#   end
# end
