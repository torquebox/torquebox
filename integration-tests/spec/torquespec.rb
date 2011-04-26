require 'pathname'
require 'net/http'

module TorqueSpec
  def deploy(*paths)
    metaclass = class << self; self; end
    metaclass.send(:define_method, :deploy_paths) do
      paths.map {|p| Pathname.new(p).absolute? ? p : File.join(TorqueSpec.knob_root, p) }
    end
  end

  class << self
    attr_accessor :host, :port, :knob_root, :jboss_home, :jboss_conf, :jvm_args, :max_heap, :lazy
    def configure
      yield self
    end
    def jvm_args
      max_heap ? @jvm_args.sub(/-Xmx\w+/, "-Xmx#{max_heap}") : @jvm_args
    end
  end
end

# Default configuration options
TorqueSpec.configure do |config|
  config.lazy = true
  config.host = 'localhost'
  config.port = 8080
  config.jboss_home = ENV['JBOSS_HOME']
  config.jboss_conf = 'default'
  config.jvm_args = "-Xmx1024m -XX:MaxPermSize=512m -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSClassUnloadingEnabled -Djruby_home.env.ignore=true -Dgem.path=default"
end


module TorqueSpec
  class Server
    
    def start(opts={})
      return if TorqueSpec.lazy and ready?
      wait = opts[:wait].to_i
      raise "JBoss is already running" if ready?
      cmd = command
      @process = IO.popen( cmd )
      Thread.new(@process) { |console| while(console.gets); end }
      %w{ INT TERM KILL }.each { |signal| trap(signal) { stop } }
      puts "#{cmd}\npid=#{@process.pid}"
      wait > 0 ? wait_for_ready(wait) : @process.pid
    end

    def deploy(url)
      t0 = Time.now
      puts "#{url}"
      success?( deployer( 'redeploy', url ) )
      puts "  deployed in #{(Time.now - t0).to_i}s"
    end

    def undeploy(url)
      success?( deployer( 'undeploy', url ) )
      puts "  undeployed #{url.split('/')[-1]}"
    end

    def stop
      return if TorqueSpec.lazy
      if @process
        unless clean_stop
          puts "Unable to shutdown JBoss cleanly, interrupting process"
          Process.kill("INT", @process.pid) 
        end
        @process = nil
        puts "JBoss stopped"
      end
    end

    def clean_stop
      success?( jmx_console( :action     => 'invokeOpByName', 
                             :name       => 'jboss.system:type=Server', 
                             :methodName => 'shutdown' ) )
    end

    def ready?
      response = jmx_console( :action => 'inspectMBean', :name => 'jboss.system:type=Server' )
      "True" == response.match(/>Started<.*?<pre>\s+^(\w+)/m)[1]
    rescue
      false
    end

    def wait_for_ready(timeout)
      puts "Waiting up to #{timeout}s for JBoss to boot"
      t0 = Time.now
      while (Time.now - t0 < timeout && @process) do
        if ready?
          puts "JBoss started in #{(Time.now - t0).to_i}s"
          return true
        end
        sleep(1)
      end
      raise "JBoss failed to start"
    end

    protected

    def command
      java_home = java.lang::System.getProperty( 'java.home' )
      "#{java_home}/bin/java -cp #{TorqueSpec.jboss_home}/bin/run.jar #{TorqueSpec.jvm_args} -Djava.endorsed.dirs=#{TorqueSpec.jboss_home}/lib/endorsed org.jboss.Main -c #{TorqueSpec.jboss_conf} -b #{TorqueSpec.host}"
    end

    def deployer(method, url)
      jmx_console( :action     => 'invokeOpByName', 
                   :name       => 'jboss.system:service=MainDeployer', 
                   :methodName => method,
                   :argType    => 'java.net.URL', 
                   :arg0       => url )
    end

    def success?(response)
      response.include?( "Operation completed successfully" )
    end

    def jmx_console(params)
      req = Net::HTTP::Post.new('/jmx-console/HtmlAdaptor')
      req.set_form_data( params )
      http( req )
    end

    def http req
      res = Net::HTTP.start(TorqueSpec.host, TorqueSpec.port) do |http| 
        http.read_timeout = 180 
        http.request(req)
      end
      unless Net::HTTPSuccess === res
        STDERR.puts res.body
        res.error!
      end
      res.body
    end

  end

end

begin
  require 'rspec'
  TorqueSpec::Configurator = RSpec
rescue Exception
  require 'spec'
  TorqueSpec::Configurator = Spec::Runner
end

TorqueSpec::Configurator.configure do |config|

  config.extend(TorqueSpec)

  config.before(:suite) do
    Thread.current[:app_server] = TorqueSpec::Server.new
    Thread.current[:app_server].start(:wait => 120)
  end
  
  config.before(:all) do
    if self.class.respond_to?( :deploy_paths )
      self.class.deploy_paths.each do |path|
        Thread.current[:app_server].deploy(path)
      end
    end
  end

  config.after(:all) do
    if self.class.respond_to?( :deploy_paths )
      self.class.deploy_paths.each do |path|
        Thread.current[:app_server].undeploy(path)
      end
    end
  end

  config.after(:suite) do
    Thread.current[:app_server].stop
  end

end

