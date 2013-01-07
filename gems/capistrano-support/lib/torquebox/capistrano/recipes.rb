# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

# @api private
module Capistrano
  class Configuration
    def create_deployment_descriptor( root )
        dd = {
          'application'=>{
            # Force the encoding to UTF-8 on 1.9 since the value may be ASCII-8BIT, which marshals as an encoded bytestream, not a String.
            'root'=>"#{root.respond_to?(:force_encoding) ? root.force_encoding('UTF-8') : root}",
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

        if ( exists?( :rails_env ) )
          dd['environment'] ||= {}
          dd['environment']['RAILS_ENV'] = rails_env
        end

	if (exists?( :stomp_host ) )
	  dd['stomp'] ||= {}
	  dd['stomp']['host'] = stomp_host
	end

        dd
    end
  end
        
  module TorqueBox

    def self.load_into( configuration )
      configuration.load do 
        # --

        set( :torquebox_home,      '/opt/torquebox' ) unless exists?( :torquebox_home )

        set( :jruby_home,          lambda{ "#{torquebox_home}/jruby" } ) unless exists?( :jruby_home )
        if exists?( :app_ruby_version ) && !exists?( :jruby_opts )
          set( :jruby_opts,          lambda{ "--#{app_ruby_version}" } )
        end
        set( :jruby_bin,           lambda{ "#{jruby_home}/bin/jruby #{jruby_opts if exists?( :jruby_opts )}" } ) unless exists?( :jruby_bin )

        set( :jboss_home,          lambda{ "#{torquebox_home}/jboss" } ) unless exists?( :jboss_home )
        set( :jboss_control_style, :initd ) unless exists?( :jboss_control_style )
        set( :jboss_init_script,   '/etc/init.d/jboss-as-standalone' ) unless exists?( :jboss_init_script )
        set( :jboss_runit_script,  '/etc/service/torquebox/run' ) unless exists?( :jboss_runit_script)
        set( :jboss_bind_address,  '0.0.0.0' ) unless exists?( :jboss_bind_address )

        set( :bundle_cmd,          lambda{ "#{jruby_bin} -S bundle" } ) unless exists?( :bundle_cmd )
        set( :bundle_flags,        '' ) unless exists?( :bundle_flags )
        
        namespace :deploy do

          desc "Restart Application"
          task :restart do
            run "touch #{jboss_home}/standalone/deployments/#{application}-knob.yml.dodeploy"
          end
 
          namespace :torquebox do

            desc "Start TorqueBox Server"
            task :start do
              puts "Starting TorqueBox AS"
              case ( jboss_control_style )
                when :initd
                  run "#{jboss_init_script} start"
                when :binscripts
                  run "nohup #{jboss_home}/bin/standalone.sh -b #{jboss_bind_address} < /dev/null > /dev/null 2>&1 &"
                when :runit
                  run "#{sudo} sv start torquebox"
              end
            end
        
            desc "Stop TorqueBox Server"
            task :stop do
              puts "Stopping TorqueBox AS"
              case ( jboss_control_style )
                when :initd
                  run "JBOSS_HOME=#{jboss_home} #{jboss_init_script} stop"
                when :binscripts
                  run "#{jboss_home}/bin/jboss-cli.sh --connect :shutdown"
                when :runit
                  run "#{sudo} sv stop torquebox"
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
                when :runit
                  puts "Restarting TorqueBox AS"
                  run "#{sudo} sv restart torquebox"
              end
            end

            task :info do
              puts "torquebox_home.....#{torquebox_home}"
              puts "jboss_home.........#{jboss_home}"
              puts "jruby_home.........#{jruby_home}"
              puts "bundle command.....#{bundle_cmd}"
            end

            task :check do
              puts "style #{jboss_control_style}"
              case jboss_control_style
              when :initd
                run "test -x #{jboss_init_script}",                      :roles=>[ :app ]
              when :runit
                run "test -x #{jboss_runit_script}",                     :roles=>[ :app ]
              end
              run "test -d #{jboss_home}",                               :roles=>[ :app ]
              unless ( [ :initd, :binscripts, :runit ].include?( jboss_control_style.to_sym ) )
                fail "invalid jboss_control_style: #{jboss_control_style}"
              end
            end

            task :deployment_descriptor do
              puts "creating deployment descriptor"
              dd_str = YAML.dump_stream( create_deployment_descriptor(latest_release) )
              dd_file = "#{jboss_home}/standalone/deployments/#{application}-knob.yml"
              cmd =  "cat /dev/null > #{dd_file}"
              dd_str.each_line do |line|
                cmd += " && echo \"#{line}\" >> #{dd_file}"
              end
              cmd += " && echo '' >> #{dd_file}"
              run cmd
            end
          end


          desc "Dump the deployment descriptor"
          task :dump do
            dd = create_deployment_descriptor( latest_release )
            puts dd
            exit
            puts YAML.dump( create_deployment_descriptor( latest_release ) )
          end

        end

        before 'deploy:check',          'deploy:torquebox:check'
        after  'deploy:create_symlink', 'deploy:torquebox:deployment_descriptor'
      end
    end
  end
end


if Capistrano::Configuration.instance
  Capistrano::TorqueBox.load_into(Capistrano::Configuration.instance)
end

