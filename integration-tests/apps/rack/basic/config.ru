
app = lambda { |env| 
  puts "Invoking app"
  [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it worked</div><div id='ruby-version'>#{RUBY_VERSION}</div>"] 
}
run app
