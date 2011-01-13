
require 'capistrano'

Capistrano::Configuration.instance.load do 
  _cset( :jboss_home,         '/opt/jboss' )
  _cset( :jboss_config,       'default'    )
  _cset( :jboss_service_name, 'jbossas'    )
  
  namespace :deploy do
  
    desc "Perform a deployment"

    task :default do
      update
    end
  
    desc "Start TorqueBox Server"
    task :start do
      puts "starting server via #{jboss_daemon_manager}"
      run "/etc/init.d/#{jboss_service_name} start"
    end
  
    desc "Stop TorqueBox Server"
    task :stop do
      puts "stopping server via #{jboss_daemon_manager}"
      run "/etc/init.d/#{jboss_service_name} stop"
    end
  
    desc "Restart TorqueBox Server"
    task :restart do
      puts "restarting server via #{jboss_daemon_manager}"
      puts "/etc/initd/#{jboss_service_name} restart"
    end
  
    task :after_symlink do
      deployment_descriptor
    end

    #desc "Emit the deployment symlink"
    task :deployment_symlink do
      symlink_path = "#{jboss_home}/server/#{jboss_config}/deploy/#{application}.rails"
      cmd = "if [ -h #{symlink_path} ] ; then "
      cmd += "rm #{symlink_path} "
      cmd += ";fi "
      cmd += "&& ln -s #{latest_release} #{symlink_path}"
      run cmd
    end
  
    task :deployment_descriptor do
      puts "creating deployment descriptor"
  
      dd = {
        'application'=>{
          'RAILS_ROOT'=>"#{latest_release}",
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

