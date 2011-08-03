require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "STOMP applications" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/stomp
      env: development
    
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should be able to connect and disconnect using pure stomp" do
    client = Stilts::Stomp::Client.new( "stomp://localhost/" );

    client.connect
    client.disconnect
  end

  it "should be able to connect and disconnect using pure stomp over websockets" do
    client = Stilts::Stomp::Client.new( "stomp+ws://localhost/" );

    client.connect
    client.disconnect
  end


  it "should be able to subscribe" do
    client = Stilts::Stomp::Client.new( "stomp://localhost/" );

    client.connect
    client.subscribe( "/queues/foo" ) do |message|
      puts "received message #{message}"
    end

    sleep( 1 )
    client.disconnect
  end

  it "should be able to subscribe send and receive" do
    client = Stilts::Stomp::Client.new( "stomp://localhost/" );

    client.connect

    received_message = nil

    client.subscribe( "/queues/foo" ) do |message|
      puts "received message #{message}"
      received_message = message
    end

    sleep( 1 )

    client.send( "/queues/foo", "this is my message" )
    sleep( 1 )

    client.disconnect
 
    received_message.should_not be_nil
    received_message.body.should eql( "this is my message" )
  end

end

