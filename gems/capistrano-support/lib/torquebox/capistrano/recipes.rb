# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'capistrano'

Capistrano::Configuration.instance.load do 

  # --

  _cset( :app_ruby_version,    1.8 )

  _cset( :torquebox_home,      '/opt/torquebox'   )
  _cset( :jboss_home,          lambda{ "#{torquebox_home}/jboss" } )
  _cset( :jruby_home,          lambda{ "#{torquebox_home}/jruby" } )
  _cset( :jruby_opts,          lambda{ "--#{app_ruby_version}" } )
  _cset( :jruby_bin,           lambda{ "#{jruby_home}/bin/jruby #{jruby_opts}" } )

  _cset( :jboss_control_style, :initid )
  _cset( :jboss_init_script,   '/etc/init.d/jboss-as-standalone' )

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
          run "nohup #{jboss_home}/bin/standalone.sh -bpublic=#{jboss_bind_address} < /dev/null > /dev/null 2>&1 &"
      end
    end
  
    desc "Stop TorqueBox Server"
    task :stop do
      puts "Stopping TorqueBox AS"
      case ( jboss_control_style )
        when :initd
          run "JBOSS_HOME=#{jboss_home} #{jboss_init_script} stop"
        when :binscripts
          run "#{jboss_home}/bin/jboss-admin.sh --connect :shutdown"
      end
    end
  
    desc "Restart TorqueBox Server"
    task :restart do
      case ( jboss_control_style )
        when :initd
          puts "Restarting TorqueBox AS"
          puts "JBOSS_HOME=#{jboss_home} #{jboss_init_script} restart"
        when :binscripts
          run "JBOSS_HOME=#{jboss_home} #{jboss_init_script} stop"
          run "nohup #{jboss_home}/bin/standalone.sh -bpublic=#{jboss_bind_address} < /dev/null > /dev/null 2>&1 &"
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
        puts "style #{jboss_control_style}"
        if ( jboss_control_style == :initd )
          run "test -x #{jboss_init_script}",                        :roles=>[ :app ]
        end
        run "test -d #{jboss_home}",                               :roles=>[ :app ]
        unless ( [ :initd, :binscripts ].include?( jboss_control_style.to_sym ) )
          fail "invalid jboss_control_style: #{jboss_control_style}"
        end
      end

      task :deployment_descriptor do
        puts "creating deployment descriptor"
        dd_str = YAML.dump_stream( create_deployment_descriptor() )
        dd_file = "#{jboss_home}/#{jboss_config}/deployments/#{application}-knob.yml"
        cmd =  "cat /dev/null > #{dd_file}"
        dd_str.each_line do |line|
          cmd += " && echo \"#{line}\" >> #{dd_file}"
        end
        cmd += " && echo '' >> #{dd_file}"
        run cmd
        run "touch #{dd_file}.dodeploy"
      end
    end


    desc "Dump the deployment descriptor"
    task :dump do
      puts YAML.dump( create_deployment_descriptor )
    end

    def create_deployment_descriptor
        dd = {
          'application'=>{
            'root'=>"#{latest_release}",
          },
        }

        if ( exists?( :app_host ) )
          dd['web'] ||= {}
          dd['web']['host'] = app_host
        end

        if ( exists?( :app_context ) )
          dd['web'] ||= {}
          dd['web']['context'] = app_context
        end

        if ( exists?( :app_ruby_version ) )
          dd['ruby'] ||= {}
          dd['ruby']['version'] = app_ruby_version
        end

        if ( exists?( :app_environment ) && ! app_environment.empty? ) 
          dd['environment'] = app_environment
        end

        dd
    end
  
  end

  before 'deploy:check',   'deploy:torquebox:check'
  after  'deploy:symlink', 'deploy:torquebox:deployment_descriptor'

end

