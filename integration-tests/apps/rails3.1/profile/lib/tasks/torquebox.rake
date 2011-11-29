begin
  require 'torquebox-rake-support'
rescue LoadError => ex
  puts "Failed to load the TorqueBox rake gem (torquebox-rake-support). Make sure it is available in your environment."
end
