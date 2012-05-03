# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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
require 'torquebox/server'


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
        else
          torquebox_home = TorqueBox::Server.torquebox_home
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

      def cluster_config_file
        "standalone-ha.xml"
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

      def archive_name(root = Dir.pwd)
        normalize_archive_name( File.basename( root || Dir.pwd ) )
      end

      def deployment_name(root = Dir.pwd)
        normalize_yaml_name( File.basename( root || Dir.pwd ) )
      end

      def check_server
        raise "#{jboss_home} doesn't appear to be a valid TorqueBox install" unless File.exist?( torquebox_modules_dir )
        puts "TorqueBox installation appears OK"
      end

      def check_opt_torquebox
        raise "TorqueBox not installed in #{opt_torquebox}" unless ( File.exist?( opt_torquebox ) )
        puts "TorqueBox install OK: #{opt_torquebox}"
      end

      def set_java_opts(options)
        ENV['APPEND_JAVA_OPTS'] = options
      end

      def run_command_line(opts={})
        options = ENV['JBOSS_OPTS'] || ''
        options = "#{options} --server-config=#{cluster_config_file}" if opts[:clustered]
        options = "#{options} -Dorg.torquebox.web.http.maxThreads=#{opts[:max_threads]}" if opts[:max_threads]
        options = "#{options} -b #{opts[:bind_address]}" if opts[:bind_address]
        options = "#{options} -Djboss.socket.binding.port-offset=#{opts[:port_offset]}" if opts[:port_offset]
        options = "#{options} -Djboss.node.name=#{opts[:node_name]}" if opts[:node_name]
        options = "#{options} -Djboss.server.data.dir=#{opts[:data_directory]}" if opts[:data_directory]
        options = "#{options} #{opts[:pass_through]}" if opts[:pass_through]
        if windows?
          cmd = "#{jboss_home.gsub('/', '\\')}\\bin\\standalone.bat"
        else
          cmd = "/bin/sh bin/standalone.sh"
        end
        puts "#{cmd} #{options}" # Make it clear to the user what is being passed through to JBoss AS
        [cmd, options]
      end

      def is_deployed?( appname = deployment_name )
        File.exists?( File.join(deploy_dir, appname) )
      end

      def run_server(options={})
        puts "[WARNING] #{deployment_name} has not been deployed. Starting TorqueBox anyway." unless ( is_deployed? )

        Dir.chdir(jboss_home) do
          # don't send the gemfile from the current app, instead let
          # bundler suss it out itself for each deployed
          # app. Otherwise, they'll end up sharing this Gemfile, which
          # is probably not what we want.
          ENV.delete('BUNDLE_GEMFILE')

          set_java_opts(options[:jvm_options])
          exec_command(run_command_line(options).join(' '))
        end
      end

      def create_archive(opts = {})

        archive = normalize_archive_name( find_option( opts, 'name' ) || archive_name )
        app_dir = find_option( opts, 'app_dir') || Dir.pwd
        dest_dir = find_option( opts, 'dest_dir') || Dir.pwd

        default_skip_files = %w{ ^log$ ^tmp$ ^test$ ^spec$ \.knob$ vendor }
        opts_skip_files = (find_option(opts, 'exclude') || "").split(/,/)
        skip_files = default_skip_files + opts_skip_files

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
          run_command( cmd )
        end

        archive_path
      end

      def freeze_gems(app_dir = Dir.pwd)
        Dir.chdir( app_dir ) do
          jruby = File.join( RbConfig::CONFIG['bindir'], RbConfig::CONFIG['ruby_install_name'] )
          jruby << " --1.9" if RUBY_VERSION =~ /^1\.9\./
          run_command( "#{jruby} -S bundle package" )
          run_command( "#{jruby} -S bundle install --local --path vendor/bundle" )
        end
      end

      def basic_deployment_descriptor(options = {})
        env = options[:env] || options['env']
        env ||= defined?(RACK_ENV) ? RACK_ENV : ENV['RACK_ENV']
        env ||= defined?(::Rails) && Rails.respond_to?(:env) ? ::Rails.env : ENV['RAILS_ENV']

        root = options[:root] || options['root'] || Dir.pwd
        context_path = options[:context_path] || options['context_path']
        
        d = {}
        d['application'] = {}
        d['application']['root'] = root
        d['environment'] = {}
        d['environment']['RACK_ENV'] = env.to_s if env

        if context_path
          d['web'] = {}
          d['web']['context'] = context_path
        end

        d
      end

      def deploy_yaml(deployment_descriptor, opts = {})
        name = normalize_yaml_name( find_option( opts, 'name' ) || deployment_name(opts[:root] || opts['root']) )
        dest_dir = opts[:dest_dir] || opts['dest_dir'] || deploy_dir
        deployment = File.join( dest_dir, name )
        File.open( deployment, 'w' ) do |file|
          YAML.dump( deployment_descriptor, file )
        end
        FileUtils.touch( dodeploy_file( name ) )
        [name, dest_dir]
      end

      def deploy_archive(opts = {})
        name = normalize_archive_name( find_option( opts, 'name' ) || archive_name )
        archive_path = find_option( opts, 'archive_path' ) || File.join( Dir.pwd, name )
        dest_dir = find_option( opts, 'dest_dir' ) || deploy_dir
        FileUtils.cp( archive_path, dest_dir )
        archive = File.basename( archive_path )
        FileUtils.touch( dodeploy_file( archive ) )
        [archive, dest_dir]
      end

      def dodeploy_file(name)
        File.join( DeployUtils.deploy_dir, "#{name}" ) + ".dodeploy"
      end

      def deployed_file(name)
        File.join( DeployUtils.deploy_dir, "#{name}" ) + ".deployed"
      end
      
      def undeploy_archive(opts = {})
        undeploy( normalize_archive_name( find_option( opts, 'name' ) || archive_name( opts[:root] ) ), opts )
      end 
      
      def undeploy_yaml(opts = {})
        undeploy( normalize_yaml_name( find_option( opts, 'name' ) || deployment_name( opts[:root] ) ), opts )
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

      # Used when we want to effectively replace this process with the
      # given command. On Windows this does call Kernel#exec but on
      # everything else we just delegate to run_command.
      #
      # This is mainly so CTRL+C, STDIN, STDOUT, and STDERR work as
      # expected across all operating systems.
      def exec_command(cmd)
        windows? ? exec(cmd) : run_command(cmd)
      end

      # Used to run a command as a subprocess
      def run_command(cmd)
        exiting = false
        IO.popen4(cmd) do |pid, stdin, stdout, stderr|
          stdout.sync = true
          stderr.sync = true
          trap("INT") do
            exiting = true
            stdin.close
            puts "caught SIGINT, shutting down"
            `taskkill /F /T /PID #{pid}` if windows?
          end

          # Don't join on stdin since interrupting a blocking read on
          # JRuby is pretty tricky
          Thread.new(stdin) { |stdin_io|
            begin
              until exiting
                stdin_io.write(STDIN.readpartial(1024))
                stdin_io.flush
              end
            rescue Errno::EBADF, IOError
            end
          }

          # Join on stdout/stderr since they'll be closed
          # automatically once TorqueBox exits
          [ Thread.new(stdout) { |stdout_io|
              begin
                while true
                  STDOUT.write(stdout_io.readpartial(1024))
                end
              rescue EOFError
              end
            },

            Thread.new(stderr) { |stderr_io|
              begin
                while true
                  STDERR.write(stderr_io.readpartial(1024))
                end
              rescue EOFError
              end
            }
          ].each( &:join)
        end

      end

      def windows?
        Config::CONFIG['host_os'] =~ /mswin/
      end
      
      def find_option(opt, key)
        opt[key.to_sym] || opt[key] || ENV[key] || ENV[key.upcase]
      end
      
      def normalize_yaml_name(name)
        name[-9..-1] == '-knob.yml' ? name : name + '-knob.yml'
      end
      
      def normalize_archive_name(name)
        name[-5..-1] == '.knob' ? name : name + '.knob'
      end

      def deployment_descriptors
        Dir.glob( "#{deploy_dir}/*-knob.yml" ).collect { |d| File.basename( d ) }
      end

      def deployment_status
        applications = {}
        deployment_descriptors.each do | descriptor |
          descriptor_path = File.join( deploy_dir, descriptor )
          appname = descriptor.sub( /\-knob.yml/, '' ) 
          applications[appname] = {}
          applications[appname][:descriptor] = descriptor_path
          applications[appname][:status] = case 
                                           when File.exists?("#{descriptor_path}.dodeploy")
                                             "awaiting deployment"
                                           when File.exists?("#{descriptor_path}.deployed")
                                             "deployed"
                                           when File.exists?("#{descriptor_path}.failed")
                                             "deployment failed"
                                           else "unknown: try running `torquebox deploy #{appname}`"
                                           end
        end
        applications
      end
      
      private 

      def undeploy(name, opts = {})
        puts "Attempting to undeploy #{name}"
        from_dir = find_option( opts, 'deploy_dir' ) || deploy_dir
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

    end
  end
end

