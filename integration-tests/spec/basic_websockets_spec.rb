require 'spec_helper'
#require 'websocket_client'

describe "basic websockets test" do

  deploy( :application => { 
            :root => "#{File.dirname(__FILE__)}/../apps/rack/websockets", 
            :env => 'development' },
          :web => { :context => '/websockets' },
          :ruby => { :version => RUBY_VERSION[0,3] } )  

  
  it "should be deployable" do
    visit( '/websockets' )
    page.find("#success")[:class].should == 'websockets'

    ws_url = page.find("#endpoint-echo").text

    ws_url.should_not be_empty

    outbound = [
      'touched by his noodly appendage',
      'france is bacon',
    ]
    inbound = []

    puts "Creating client to #{ws_url}"
    WebSocketClient.create( ws_url ) do |client|
      client.on_message do |message|
        puts "received: #{message}"
        inbound << message
      end

      puts "Connecting client"
     
      client.connect

      puts "Connected client"

     
      outbound.each do |e|  
        puts "Sending #{e}"
        client.send( e )
      end
      sleep(1)
    end

    puts inbound.inspect

    inbound.should_not be_empty

    outbound.each do |e|
      inbound.should include( "ECHO:#{e}" )
    end

  end

end

