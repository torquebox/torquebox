#!/usr/bin/env jruby

require 'rubygems'
require 'torquebox-messaging-client'

#TorqueBox::Messaging::Client.connect do |client|
  #1.upto( 10 ) do |i|
    client.send( '/queues/foo', "The message #{Time.now}" )
  #end
#end


connection = TorqueBox::Messaging::Connection.new()

session = connection.create_session( true )

session.message_listener = MyMessageListener.new


