
require 'capistrano'

def is_rails?
  return true if ( File.exists?( 'config/deploy.rb' ) )  
  false
end

Capistrano::Configuration.instance.load do 

  _cset( :torquebox_home,      '/opt/torquebox'   )
  _cset( :jboss_home,          lambda{ "#{torquebox_home}/jboss" } )
  _cset( :jruby_home,          lambda{ "#{torquebox_home}/jruby" } )
  _cset( :jruby_bin,           lambda{ "#{jruby_home}/bin/jruby" } )

  _cset( :jboss_config,        'default'      )

  _cset( :jboss_control_style, :initid )
  _cset( :jboss_init_script,   '/etc/init.d/jbossas' )

  _cset( :jboss_bind_address,  '0.0.0.0'      )

  _cset( :bundle_cmd,          lambda{ "#{jruby_bin} -S bundle" } )
  _cset( :bundle_flags,        '' )
  
  namespace :deploy do
  
    desc "Perform a deployment"

    task :default do
      update
    end
  
    desc "Start TorqueBox Server"
    task :start do
      puts "Starting TorqueBox AS"
      case ( jboss_control_style )
        when :initd
          run "#{jboss_init_script} start"
        when :binscripts
          run "nohup #{jboss_home}/bin/run.sh -b #{jboss_bind_address} -c #{jboss_config} < /dev/null > /dev/null 2>&1 &"
      end
    end
  
    desc "Stop TorqueBox Server"
    task :stop do
      puts "Stopping TorqueBox AS"
      case ( jboss_control_style )
        when :initd
          run "JBOSS_HOME=#{jboss_home} #{jboss_init_script} stop"
        when :binscripts
          run "#{jboss_home}/bin/shutdown.sh -S"
      end
    end
  
    desc "Restart TorqueBox Server"
    task :restart do
      case ( jboss_control_style )
        when :initd
          puts "Restarting TorqueBox AS"
          puts "JBOSS_HOME=#{jboss_home} #{jboss_init_script} restart"
        when :binscripts
          puts "deploy:restart only supported with initd control style."
          puts "Please use deploy:stop and deploy:start."
      end
    end
  
    namespace :torquebox do

      task :info do
        puts "torquebox_home.....#{torquebox_home}"
        puts "jboss_home.........#{jboss_home}"
        puts "jruby_home.........#{jruby_home}"
        puts "bundle command.....#{bundle_cmd}"
      end

      task :check do
        run "test -x #{jboss_init_script}",                        :roles=>[ :app ]
        run "test -d #{jboss_home}",                               :roles=>[ :app ]
        run "test -d #{jboss_home}/server/#{jboss_config}",        :roles=>[ :app ]
        run "test -w #{jboss_home}/server/#{jboss_config}/deploy", :roles=>[ :app ]
        unless ( [ :initd, :binscripts ].include?( jboss_control_style.to_sym ) )
          fail "invalid jboss_control_style: #{jboss_control_style}"
        end
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

