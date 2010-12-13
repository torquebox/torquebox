
app = lambda { |env| 
  puts "Invoking app"
  [200, { 'Content-Type' => 'text/html' }, '<div id="success" class="basic-rack">it worked</div>'] 
}
run app
