
app = lambda { |env| 
  puts "Invoking app"
  body = <<EOB
<div id='success' class='#{ENV['biscuit']} #{ENV['HAM']} #{ENV['FOO']} env-#{RACK_ENV}'>it worked</div>
<div id='ruby-version'>#{RUBY_VERSION}</div>
<div id='dir'>#{ENV['dir']}</div>
EOB
  [200, { 'Content-Type' => 'text/html' }, body] 
}
run app
