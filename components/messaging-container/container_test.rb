#!/usr/bin/env jruby

require 'rubygems'
require 'torquebox-messaging-container'

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
