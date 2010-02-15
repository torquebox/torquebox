
puts "111: Loading RUNTIME" 
require 'torquebox-messaging-runtime'

puts "222: Loading container .jar" 

require File.dirname(__FILE__) + '/../java-lib/torquebox-messaging-container.jar'

puts "333: Loading container .rb" 
require 'torquebox/messaging/container'
puts "444: complete"

