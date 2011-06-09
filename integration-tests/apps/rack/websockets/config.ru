
app = lambda { |env| 
  puts "Invoking app"
  session = env['servlet_request'].session
  session.setAttribute( 'food', "tacos" )
  puts "Session: #{session}"
  [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it worked</div><div id='ruby-version'>#{RUBY_VERSION}</div>"] 
}
run app
