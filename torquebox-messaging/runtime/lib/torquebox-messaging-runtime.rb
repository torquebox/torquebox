
puts "Loading torquebox-messaging-runtime"

require 'java'
if ( ! defined?( Java::javax.jms.Connection ) )
  puts "JMS not loaded, loading all jars"
  Dir[ File.dirname(__FILE__) + '/../java-lib/**/*.jar' ].each do |jar|
    puts "requiring #{jar}"
    require jar
  end
else
  puts "JMS found, loading no jars"
end
