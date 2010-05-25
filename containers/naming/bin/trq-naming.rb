#!/usr/bin/env jruby

require 'rubygems'

require 'org.torquebox.torquebox-naming-container'

require 'torquebox/container/naming'

container = TorqueBox::Container::Naming.new

puts "starting container"
container.start()
puts "started container"

interrupted = false
trap( "INT" ) do
  puts "shutting down container"
  container.stop
  puts "completed shut-down of container"
  interrupted = true
end

while ( ! interrupted )
  sleep( 2 )
end
