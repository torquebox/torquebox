require 'torquebox/web_sockets'

app = lambda { |env| 
  puts "Invoking app"

  echo_endpoint        = TorqueBox::WebSockets.lookup('echo')
  echo_french_endpoint = TorqueBox::WebSockets.lookup('echo-french')
  time_endpoint        = TorqueBox::WebSockets.lookup('time')

  session = env['servlet_request'].session
  session.setAttribute( 'food', "tacos" )

  puts "Session: #{session}"

  [200, { 'Content-Type' => 'text/html' }, 
    "<div class='websockets' id='success'>" +
    "  <div class='endpoint' id='endpoint-echo'>#{echo_endpoint}</div>" +
    "  <div class='endpoint' id='endpoint-echo-french'>#{echo_french_endpoint}</div>" +
    "  <div class='endpoint' id='endpoint-time'>#{time_endpoint}</div>" +
    "</div>"
  ] 
}
run app
