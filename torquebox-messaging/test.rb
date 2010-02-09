#!/usr/bin/env jruby

require 'java'

GEM_DIR = File.dirname( __FILE__ ) + '/target/gem-stage'

$: << GEM_DIR + '/lib'

Dir[ GEM_DIR + '/lib/*.jar' ].each do |jar|
  puts "require #{jar}"
  require jar
end

require 'torquebox/messaging/container'

class MyAgent
  def on_message(msg)
    puts "MyAgent just received #{msg.text}"
  end
end

container = TorqueBox::Messaging::Container.new {
  agents {
    map '/queues/foo', 'MyAgent'
  }
}

puts "container is #{container} #{container.inspect}"
container.create
container.start

container.wait_until( 'INT' )
