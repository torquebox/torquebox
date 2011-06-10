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

    #WebSocketClient.create( 'ws://localhost:8081/websockets/' ) do |client|
    WebSocketClient.create( ws_url ) do |client|
      client.on_message do |message|
        puts "received: #{message}"
        inbound << message
      end
     
      client.connect

     
      outbound.each do |e|  
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

