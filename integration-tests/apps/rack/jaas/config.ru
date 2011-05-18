
require 'torquebox-security'

app = lambda { |env| 
  req = Rack::Request.new(env)
  puts "Invoking app"
  if req.path_info == "/success"
    [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it worked</div>"] 
  else
    [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it failed</div>"] 
  end
}
run app

