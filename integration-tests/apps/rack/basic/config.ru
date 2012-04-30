
app = lambda { |env| 
  puts "Invoking app"
  [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it worked</div><div id='ruby-version'>#{RUBY_VERSION}</div><div id='path'>#{__FILE__}</div><div id='context'>#{ENV['TORQUEBOX_CONTEXT']}</div>"] 
}
run app
