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

require 'tmpdir'
require 'rbconfig'
require 'yaml'
require 'rake'


module TorqueBox
  module DeployUtils
    class << self

      def jboss_home
        jboss_home = File.expand_path(ENV['JBOSS_HOME']) if ENV['JBOSS_HOME']
        jboss_home ||= File.join(File.expand_path(ENV['TORQUEBOX_HOME']), "jboss") if ENV['TORQUEBOX_HOME']
        raise "$JBOSS_HOME is not set" unless jboss_home
        return jboss_home
      end

      def torquebox_home
        torquebox_home = nil
        if ( ENV['TORQUEBOX_HOME'] )
          torquebox_home = File.expand_path(ENV['TORQUEBOX_HOME'])
        end
        torquebox_home
      end

      def jboss_conf
        ENV['TORQUEBOX_CONF'] || ENV['JBOSS_CONF'] || 'standalone'
      end

      # TODO: This is not windows friendly, is it?
      def sys_root
        '/'
      end

      # Used by upstart and launchd
      def opt_dir
        File.join( sys_root, 'opt' )
      end

      def opt_torquebox
        File.join( opt_dir, 'torquebox' )
      end

      def server_dir
        File.join("#{jboss_home}","#{jboss_conf}" )
      end

      def config_dir
        File.join("#{server_dir}","configuration")
      end

      def properties_dir
        config_dir
      end

      def deploy_dir
        d = File.join( torquebox_home, 'apps' )
        if ( File.exists?( d ) && File.directory?( d ) )
          return d
        end

        File.join( "#{server_dir}", "deployments" )
      end

      def deployers_dir
        raise "Deployers directory no longer relevant"
      end

      def modules_dir
        File.join( jboss_home, 'modules' )
      end

      def torquebox_modules_dir
        File.join( modules_dir, 'org', 'torquebox' )
      end

      def archive_name(root=Dir.pwd)
        File.basename( root ) + '.knob'
      end

      def deployment_name(root = Dir.pwd)
        File.basename( root ) + '-knob.yml'
      end

      def check_server
        raise "No TorqueBox modules installed in #{deployers_dir}" unless File.exist?( torquebox_modules_dir )
        puts "TorqueBox installation appears OK"
      end

      def check_opt_torquebox
        raise "TorqueBox not installed in #{opt_torquebox}" unless ( File.exist?( opt_torquebox ) )
        puts "TorqueBox install OK: #{opt_torquebox}"
      end

      def run_command_line
        options = ENV['JBOSS_OPTS']
        if windows?
          cmd = "#{jboss_home.gsub('/', '\\')}\\bin\\standalone.bat"
        else
          cmd = "/bin/sh bin/standalone.sh"
        end
        [cmd, options]
      end

      def run_server
        Dir.chdir(jboss_home) do
          # don't send the gemfile from the current app, instead let
          # bundler suss it out itself for each deployed
          # app. Otherwise, they'll end up sharing this Gemfile, which
          # is probably not what we want.
          ENV.delete('BUNDLE_GEMFILE')

          if windows?
            exec *run_command_line
          else
            old_trap = trap("INT") do
              puts "caught SIGINT, shutting down"
            end
            exec_command(run_command_line.join(' '))
            trap("INT", old_trap)
          end
        end
      end


      def create_archive(archive = archive_name, app_dir = Dir.pwd, dest_dir = Dir.pwd)
        skip_files = %w{ ^log$ ^tmp$ ^test$ ^spec$ \.knob$ vendor }

        archive_path = File.join(dest_dir, archive)
        
        Dir.chdir( app_dir ) do
          include_files = []
          Dir[ "*", ".bundle" ].each do |entry|
            entry = File.basename( entry )
            unless ( skip_files.any?{ |regex| entry.match(regex)} )
              include_files << entry
            end
          end

          Dir[ 'vendor/*' ].each do |entry|
            include_files << entry unless ( entry == 'vendor/cache' )
          end

          cmd = "jar cvf #{archive_path} #{include_files.join(' ')}"
          exec_command( cmd )
        end

        archive_path
      end

      def freeze_gems(app_dir = Dir.pwd)
        Dir.chdir( app_dir ) do
          jruby = File.join( RbConfig::CONFIG['bindir'], RbConfig::CONFIG['ruby_install_name'] )
          jruby << " --1.9" if RUBY_VERSION =~ /^1\.9\./
          exec_command( "#{jruby} -S bundle package" )
          exec_command( "#{jruby} -S bundle install --local --path vendor/bundle" )
        end
      end

      def basic_deployment_descriptor(options = {})
        env = options[:env]
        env ||= defined?(RACK_ENV) ? RACK_ENV : ENV['RACK_ENV']
        env ||= defined?(::Rails) ? ::Rails.env : ENV['RAILS_ENV']

        root = options[:root] || Dir.pwd
        context_path = options[:context_path]
        
        d = {}
        d['application'] = {}
        d['application']['root'] = root
        d['application']['env'] = env.to_s if env

        if !context_path &&
            !(File.exists?( File.join( root, "torquebox.yml" )) ||
              File.exists?( File.join( root, "config", "torquebox.yml" ) ))
          context_path = '/'
        end

        if context_path
          d['web'] = {}
          d['web']['context'] = context_path
        end

        d
      end

      def deploy_yaml(deployment_descriptor, name = deployment_name, dest_dir = deploy_dir)
        deployment = File.join( dest_dir, name )
        File.open( deployment, 'w' ) do |file|
          YAML.dump( deployment_descriptor, file )
        end
        FileUtils.touch( dodeploy_file( name ) )
        [name, dest_dir]
      end

      def deploy_archive(archive_path = nil, dest_dir = deploy_dir)
        archive_path ||= File.join( Dir.pwd, archive_name )
        FileUtils.cp( archive_path, dest_dir )
        archive = File.basename( archive_path )
        FileUtils.touch( dodeploy_file( archive ) )
        [archive, dest_dir]
      end

      def dodeploy_file( name )
        File.join( DeployUtils.deploy_dir, "#{name}" ) + ".dodeploy"
      end

      def deployed_file( name )
        File.join( DeployUtils.deploy_dir, "#{name}" ) + ".deployed"
      end

      def undeploy(name = deployment_name, from_dir = deploy_dir)
        deployment = File.join( from_dir, name )
        undeployed = false
        if File.exists?( dodeploy_file( name ) )
          FileUtils.rm_rf( dodeploy_file( name ) )
          undeployed = true
        end
        if File.exists?( deployed_file( name ) )
          FileUtils.rm_rf( deployed_file( name ) )
          undeployed = true
        end
        if File.exists?( deployment )
          FileUtils.rm_rf( deployment )
          undeployed = true
        end

        if undeployed
          [name, from_dir]
        else
          puts "Can't undeploy #{deployment}. It does not appear to be deployed."
        end
      end

      def write_credentials(user_data)
        properties_file = "#{properties_dir}/torquebox-users.properties"
        roles_file      = "#{properties_dir}/torquebox-roles.properties"
        FileUtils.touch( properties_file ) unless File.exist?( properties_file )
        FileUtils.touch( roles_file ) unless File.exist?( roles_file )
        users = File.readlines( properties_file ).inject( {} ) do |accum, line|
          user, pass = line.split( '=' )
          accum[user] = pass
          accum
        end

        users = user_data.inject( users ) do |accum, user|
          accum[user[0]] = user[1]
          accum
        end
        
        File.open( properties_file, 'w' ) do |file|
          users.each do |user, pass|
            file.puts( "#{user}=#{pass}" )
          end
        end
        properties_file
      end
      
      # TODO: This is not windows friendly
      def create_symlink
        unless File.exist? opt_dir
          success = true
          if !File.writable?( sys_root )
            puts "Cannot write to #{sys_root}. Please ensure #{opt_torquebox} points to your torquebox installation."
            success = false
          else
            puts "Creating #{opt_dir}"
            Dir.new( opt_dir )
          end
        end

        unless File.exist?( opt_torquebox )
          if File.writable?( opt_dir )
            puts "Linking #{opt_torquebox} to #{torquebox_home}"
            File.symlink( torquebox_home, opt_torquebox )
          else
            puts "Cannot link #{opt_torquebox} to #{torquebox_home}"
            success = false
          end
        end
        success
      end

      def exec_command(cmd)
        IO.popen4( cmd ) do |pid, stdin, stdout, stderr|
          stdin.close
          [
           Thread.new(stdout) {|stdout_io|
             stdout_io.each_line do |l|
               STDOUT.puts l
               STDOUT.flush
             end
             stdout_io.close
           },

           Thread.new(stderr) {|stderr_io|
             stderr_io.each_line do |l|
               STDERR.puts l
               STDERR.flush
             end
           }
          ].each( &:join )
        end

      end

      def windows?
        Config::CONFIG['host_os'] =~ /mswin/
      end

    end
  end
end

