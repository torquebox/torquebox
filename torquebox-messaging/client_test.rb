#!/usr/bin/env jruby

require 'java'

GEM_DIR = File.dirname( __FILE__ ) + '/target/gem-stage'

$: << GEM_DIR + '/lib'

Dir[ GEM_DIR + '/lib/*.jar' ].each do |jar|
  puts "require #{jar}"
  require jar
end

require 'torquebox/messaging/client'

TorqueBox::Messaging::Client.new do
  1.upto( 10 ) do |i|
    send( '/queues/foo', "howdy #{i} #{Time.now}" )
  end
end

