#!/usr/bin/env jruby

require 'rubygems'

require 'vfs'
require 'org.torquebox.torquebox-container-foundation'
require 'org.torquebox.torquebox-naming-container'

require 'torquebox/naming/naming_service'

container = TorqueBox::Container::Foundation.new
container.enable( TorqueBox::Naming::NamingService ) 
begin
  container.start
rescue => e
  puts e
  puts e.backtrace
  raise e
end

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
