require 'open3'
require 'tmpdir'
require 'rbconfig'

module TorqueBox
  module RakeUtils
    def self.jboss_home
      jboss_home = ENV['JBOSS_HOME'] || ENV['TORQUEBOX_HOME'] && ENV['TORQUEBOX_HOME'] + '/jboss'
      raise "$JBOSS_HOME is not set" unless jboss_home
      return jboss_home
    end
    def self.jboss_conf
      ENV['TORQUEBOX_CONF'] || ENV['JBOSS_CONF'] || 'default'
    end
    def self.server_dir
      "#{jboss_home}/server/#{jboss_conf}"
    end
    def self.deploy_dir
      "#{server_dir}/deploy"
    end
    def self.deployers_dir
      "#{server_dir}/deployers"
    end
    def self.command_line
      cmd = Config::CONFIG['host_os'] =~ /mswin/ ? "bin\\run" : "/bin/sh bin/run.sh"
      options = ENV['JBOSS_OPTS']
      cmd += " -b 0.0.0.0" unless /((^|\s)-b\s|(^|\s)--host=)/ =~ options
      "#{cmd} -c #{jboss_conf} #{options}"
    end
    def self.run_server()
      Dir.chdir(jboss_home) do
        old_trap = trap("INT") do
          puts "caught SIGINT, shutting down"
        end
        pid = Open3.popen3( command_line ) do |stdin, stdout, stderr|
          #stdin.close
          threads = []
          threads << Thread.new(stdout) do |input_str|
            while ( ( l = input_str.gets ) != nil )
              puts l
            end
          end
          threads << Thread.new(stderr) do |input_str|
            while ( ( l = input_str.gets ) != nil )
              puts l
            end
          end
          threads.each{|t|t.join}
        end
        trap("INT", old_trap )
      end
    end
    def self.deploy_yaml(deployment_name, deployment_descriptor)
      deployment = File.join( deploy_dir, deployment_name )
      File.open( deployment, 'w' ) do |file|
        YAML.dump( deployment_descriptor, file )
      end
    end

    def self.undeploy(deployment_name)
       deployment = "#{deploy_dir}/#{deployment_name}"
       FileUtils.rm_rf( deployment )
    end
  end
end

