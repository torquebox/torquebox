
require 'capistrano'

def is_rails?
  return true if ( File.exists?( 'config/deploy.rb' ) )  
  false
end

Capistrano::Configuration.instance.load do 
  _cset( :jboss_home,         '/opt/jboss'   )
  _cset( :jboss_config,       'default'      )
  _cset( :jboss_init_script,  '/etc/init.d/jbossas' )
  
  namespace :deploy do
  
    desc "Perform a deployment"

    task :default do
      update
    end
  
    desc "Start TorqueBox Server"
    task :start do
      puts "Starting TorqueBox AS"
      run "#{jboss_init_script} start"
    end
  
    desc "Stop TorqueBox Server"
    task :stop do
      puts "Stopping TorqueBox AS"
      run "#{jboss_init_script} stop"
    end
  
    desc "Restart TorqueBox Server"
    task :restart do
      puts "Restarting TorqueBox AS"
      puts "#{jboss_init_script} restart"
    end
  
    namespace :torquebox do

      task :check do
        run "test -x #{jboss_init_script}",                        :roles=>[ :app ]
        run "test -d #{jboss_home}",                               :roles=>[ :app ]
        run "test -d #{jboss_home}/server/#{jboss_config}",        :roles=>[ :app ]
        run "test -w #{jboss_home}/server/#{jboss_config}/deploy", :roles=>[ :app ]
      end

      task :deployment_descriptor do
        puts "creating deployment descriptor"

        root_key = is_rails? ? 'RAILS_ROOT' : 'RACK_ROOT'
    
        dd = {
          'application'=>{
            root_key=>"#{latest_release}",
          },
        }
    
        dd_str = YAML.dump_stream( dd )

        dd_suffix = is_rails? ? 'rails' : 'rack'
    
        dd_file = "#{jboss_home}/server/#{jboss_config}/deploy/#{application}-#{dd_suffix}.yml"
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

  before 'deploy:check',   'deploy:torquebox:check'
  after  'deploy:symlink', 'deploy:torquebox:deployment_descriptor'

end

