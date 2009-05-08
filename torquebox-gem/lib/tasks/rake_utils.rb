require 'open3'
require 'tmpdir'

module JBoss
  module RakeUtils
    def self.jboss_home
      jboss_home = ENV['JBOSS_HOME']
      raise "$JBOSS_HOME is not set" unless jboss_home
      return jboss_home
    end
    def self.jboss_conf
      ENV['JBOSS_CONF'] || 'default'
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
    def self.deployer
      matches = Dir[ "#{deployers_dir}/jboss-rails*deployer*" ]
      raise "no deployer" if matches.empty?
      raise "too many matching deployers" if ( matches.size > 1 )
      return matches[0]
    end
    def self.run_server()
      Dir.chdir(jboss_home) do
        old_trap = trap("INT") do
          puts "caught SIGINT, shutting down"
        end
        pid = Open3.popen3( "/bin/sh bin/run.sh -c #{jboss_conf}" ) do |stdin, stdout, stderr|
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
    def self.explode_deployer()
      if ( File.directory?( deployer ) )
        return deployer
      else
        explode_dir = Dir.tmpdir + "/jboss-rails.deployer"
        if ( File.exist?( explode_dir ) )
          FileUtils.rm_rf( explode_dir )
        end
        FileUtils.mkdir_p( explode_dir )
        Dir.chdir( explode_dir ) do
          `jar xvf #{deployer}`
          raise "Unable to expand deployer" if $? != 0
        end
        return explode_dir
      end
    end
    def self.deploy(app_name, rails_root)
      deployment_descriptor = {
        'application' => {
          'RAILS_ROOT'=>rails_root,
          'RAILS_ENV'=>RAILS_ENV,
        },
        'web' => {
          'context'=>'/'
        }
      }

      deployment = "#{deploy_dir}/#{app_name}-rails.yml"
      File.open( deployment, 'w' ) do |file|
        YAML.dump( deployment_descriptor, file )
      end
    end
    def self.undeploy(app_name)
       deployment = "#{deploy_dir}/#{app_name}-rails.yml"
       FileUtils.rm_rf( deployment )
    end
  end
end
