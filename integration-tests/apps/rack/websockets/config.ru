require 'torquebox/session/servlet_store'

app = lambda { |env| 

  puts ">> Caching endpoints"
  echo_endpoint        = TorqueBox::WebSockets.lookup('echo')
  echo_french_endpoint = TorqueBox::WebSockets.lookup('echo-french')
  time_endpoint        = TorqueBox::WebSockets.lookup('time')
  session_endpoint     = TorqueBox::WebSockets.lookup('session')

  puts ">> About to grab session"
  session = env['rack.session']
  session[:food] ||= 'tacos'

  [200, { 'Content-Type' => 'text/html' }, 
    "<div class='websockets' id='success'>" +
    "  <div class='endpoint' id='endpoint-echo'>#{echo_endpoint}</div>" +
    "  <div class='endpoint' id='endpoint-echo-french'>#{echo_french_endpoint}</div>" +
    "  <div class='endpoint' id='endpoint-time'>#{time_endpoint}</div>" +
    "  <div class='endpoint' id='endpoint-session'>#{session_endpoint}#{session.url_suffix}</div>" +
    "  <div id='food'>#{session[:food]}</div>" +
    "</div>"
  ] 
}

use TorqueBox::Session::ServletStore
run app
