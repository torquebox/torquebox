
require 'torquebox-security'

app = lambda { |env| 

  puts "Invoking app"
  puts "Looking up auth domain: torquebox.authentication.jaas_authentication_tests.test-jaas" 
  service_name = TorqueBox::ServiceRegistry.service_name_for( 'torquebox.authentication.jaas_authentication_tests.test-jaas' )
  puts "Getting authenticator for service name: " + service_name.inspect
  authenticator = TorqueBox::ServiceRegistry.lookup( service_name )
  puts "Got authenticator: " + authenticator.inspect
  authenticator.authenticate( 'foo', 'bar' )

  req = Rack::Request.new(env)
  if req.path_info == "/success"
    [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it worked</div>"] 
  else
    [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it failed</div>"] 
  end
}
run app

