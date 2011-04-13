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
    attr_accessor :host, :port, :knob_root, :jboss_home, :jboss_conf, :jvm_args
    def configure
      yield self
    end
  end

  # Default configuration options
  configure do |config|
    config.host = 'localhost'
    config.port = 8080
    config.jboss_home = ENV['JBOSS_HOME']
    config.jboss_conf = 'default'
    config.jvm_args = "-Xmx1024m -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSClassUnloadingEnabled -Djruby_home.env.ignore=true -Dgem.path=default"
  end

  class Server
    
    def start(opts={})
      wait = opts[:wait].to_i
      raise "JBoss is already running" if ready?
      cmd = command
      @process = IO.popen( cmd )
      %w{ INT TERM KILL }.each { |signal| trap(signal) { stop } }
      puts cmd
      puts "pid=#{@process.pid}"
      if (wait > 0)
        wait_for_ready(wait)
      else
        @process
      end
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
      if @process
        Process.kill("INT", @process.pid) 
        @process = nil
        puts "JBoss stopped"
      end
    end

    def ready?
      response = jmx_console( :action => 'inspectMBean', :name => 'jboss.system:type=Server' )
      "True" == response.match(/>Started<.*?<pre>\n(\w+)/m)[1]
    rescue
      nil
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
      res = Net::HTTP.start(TorqueSpec.host, TorqueSpec.port) {|http| http.request(req) }
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
    self.class.deploy_paths.each do |path|
      Thread.current[:app_server].deploy(path)
    end
  end

  config.after(:all) do
    self.class.deploy_paths.each do |path|
      Thread.current[:app_server].undeploy(path)
    end
  end

  config.after(:suite) do
    Thread.current[:app_server].stop
  end

end

