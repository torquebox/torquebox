require 'net/http'

class JBossAppServer
  
  def start(opts={})
    wait = opts[:wait].to_i
    process = IO.popen( "#{ENV['JBOSS_HOME']}/bin/run.sh" )
    if (wait > 0)
      wait_for_ready(wait)
    else
      process
    end
  end

  def deploy(url)
    success?( deployer( 'redeploy', url ) )
  end

  def undeploy(url)
    success?( deployer( 'undeploy', url ) )
  end

  def stop
    success?( jmx_console( :action     => 'invokeOpByName', 
                           :name       => 'jboss.system:type=Server', 
                           :methodName => 'shutdown' ) )
  end

  def ready?
    response = jmx_console( :action => 'inspectMBean', :name => 'jboss.system:type=Server' )
    "True" == response.match(/>Started<.*?<pre>\n(\w+)/m)[1]
  rescue
    nil
  end

  def wait_for_ready(timeout)
    puts "Waiting #{timeout} seconds for complete boot"
    t0 = Time.now
    while (Time.now - t0 < timeout) do
      break if ready?
      sleep(1)
    end
    ready?
  end


  protected

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
    res = Net::HTTP.start('localhost', 8080) {|http| http.request(req) }
    unless Net::HTTPSuccess === res
      STDERR.puts res.body
      res.error!
    end
    res.body
  end

end
