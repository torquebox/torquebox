extend TorqueBox::Injectors
service = fetch( 'service:LoopyService' )

puts "Outside Service is: #{service}"

app = lambda { |env| 
  puts "Invoking app"
  puts "Inside Service is: #{service}"

  sleep( 2 )

  [200, 
   { 'Content-Type' => 'text/html' }, 
   "<div id='success' class='service-injection'>#{service.num_loops}</div>"
  ] 
}
run app
