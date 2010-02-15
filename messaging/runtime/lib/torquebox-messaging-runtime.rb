
puts "Loading torquebox-messaging-runtime"

require 'java'

begin
  Java::javax.jms.Connection
  puts "JMS found, loading no jars"
rescue NameError=>e
  puts "JMS not loaded, loading all jars"
  Dir[ File.dirname(__FILE__) + '/../java-lib/**/*.jar' ].each do |jar|
    puts "requiring #{jar}"
    require jar
  end
end
