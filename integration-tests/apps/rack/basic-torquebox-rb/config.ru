
app = lambda { |env| 
  puts "Invoking app"
  [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='#{ENV['biscuit']} #{ENV['HAM']} #{ENV['FOO']} env-#{RACK_ENV}'>it worked</div><div id='ruby-version'>#{RUBY_VERSION}</div>"] 
}
run app
