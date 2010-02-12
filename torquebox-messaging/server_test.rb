#!/usr/bin/env jruby

require 'java'

GEM_DIR = File.dirname( __FILE__ ) + '/target/gem-stage'

$: << GEM_DIR + '/lib'

Dir[ GEM_DIR + '/lib/*.jar' ].each do |jar|
  require jar
end

require 'torquebox/messaging/container'

class MyConsumer
  attr_accessor :session

  def on_message(msg)
    puts "Received: #{msg.text}"
  end
end

container = TorqueBox::Messaging::Container.new {

  naming_provider_url 'jnp://localhost:1099/'

  consumers {
    map '/queues/foo', MyConsumer
  }
}

container.start
container.wait_until( 'INT' )
