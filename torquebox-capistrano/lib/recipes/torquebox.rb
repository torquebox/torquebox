
require 'capistrano'

Capistrano::Configuration.instance.load do 
  _cset( :jboss_daemon_manager, :daemontools )
  _cset( :jboss_service_directory ) { "/service/#{application}" }
  _cset( :jboss_service_name, :jboss )
  _cset( :rails_env, 'production' )
  
  
  namespace :deploy do
  
    desc "Perform a deployment"
    task :default do
      update
    end
  
    desc "Start TorqueBox Server"
    task :start do
      case ( jboss_daemon_manager )
        when :daemontools
          puts "starting server via #{jboss_daemon_manager}"
          run "svc -u #{jboss_service_directory}"
        when :initd
          puts "starting server via #{jboss_daemon_manager}"
          run "/etc/init.d/#{jboss_service_name} start"
      end
    end
  
    desc "Stop TorqueBox Server"
    task :stop do
      case ( jboss_daemon_manager )
        when :daemontools
          puts "stopping server via #{jboss_daemon_manager}"
          run "svc -d #{jboss_service_directory}"
        when :initd
          puts "stopping server via #{jboss_daemon_manager}"
          run "/etc/init.d/#{jboss_service_name} stop"
      end
    end
  
    desc "Restart TorqueBox Server"
    task :restart do
      case ( jboss_daemon_manager )
        when :daemontools
          puts "restarting server via #{jboss_daemon_manager}"
          puts "svc -tu #{jboss_service_directory}"
        when :initd
          puts "restarting server via #{jboss_daemon_manager}"
          puts "/etc/initd/#{jboss_service_name} restart"
      end
    end
  
    task :after_symlink do
      #deployment_descriptor
    end
  
    desc "Emit #{application}-rails.yml"
    task :deployment_descriptor do
      puts "creating deployment descriptor"
  
      dd = {
        'application'=>{
          'RAILS_ENV'=>rails_env,
          'RAILS_ROOT'=>"#{latest_release}",
        },
        'web'=>{
          'context'=>'/'
        },
      }
  
      dd_str = YAML.dump_stream( dd )
  
      dd_file = "#{jboss_home}/server/#{jboss_config}/deploy/#{application}-rails.yml"
      dd_tmp_file = "#{dd_file}.tmp"
      
      cmd =  "cat /dev/null > #{dd_tmp_file}"
  
      dd_str.each_line do |line|
        cmd += " && echo \"#{line}\" >> #{dd_tmp_file}"
      end
  
      cmd += " && mv #{dd_tmp_file} #{dd_file}"
  
      run cmd
    end
  
  end
end

