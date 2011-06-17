require 'spec_helper'
#require 'websocket_client'

describe "basic websockets test" do

  deploy( :application => { 
            :root => "#{File.dirname(__FILE__)}/../apps/rack/websockets", 
            :env => 'development' },
          :web => { :context => '/websockets' },
          :ruby => { :version => RUBY_VERSION[0,3] } )  

  
  it "should be work with request/response cycles" do
    pending('until bob fixes websockets')
    visit( '/websockets' )
    page.find("#success")[:class].should == 'websockets'

    ws_url = page.find("#endpoint-echo").text

    ws_url.should_not be_empty

    outbound = [
      'touched by his noodly appendage',
      'france is bacon',
    ]
    inbound = []

    WebSocketClient.create( ws_url ) do |client|
      client.on_message do |message|
        inbound << message
      end

      client.connect

      outbound.each do |e|  
        client.send( e )
      end
      sleep(1)
    end

    inbound.should_not be_empty

    outbound.each do |e|
      inbound.should include( "ECHO:#{e}" )
    end
  end

  it "should be work with request/response cycles with config" do
    pending('until bob fixes websockets')
    visit( '/websockets' )
    page.find("#success")[:class].should == 'websockets'

    ws_url = page.find("#endpoint-echo-french").text

    ws_url.should_not be_empty

    outbound = [
      'yo dawg, I heard you liked a socket in your socket',
      'it went *okay*',
    ]
    inbound = []

    WebSocketClient.create( ws_url ) do |client|
      client.on_message do |message|
        inbound << message
      end

      client.connect

      outbound.each do |e|
        client.send( e )
      end
      sleep(1)
    end

    inbound.should_not be_empty

    outbound.each do |e|
      inbound.should include( "L'ECHO:#{e}" )
    end
  end

  it "should be work with server-initiated communication" do
    pending('until bob fixes websockets')
    visit( '/websockets' )
    page.find("#success")[:class].should == 'websockets'

    ws_url = page.find("#endpoint-time").text

    ws_url.should_not be_empty

    inbound = []

    WebSocketClient.create( ws_url ) do |client|
      client.on_message do |message|
        inbound << message
      end

      client.connect

      sleep(2)
    end

    inbound.size.should eql(1)

  end

  it "should be able to throw sessions across with a matrix parameter" do
    pending('until bob fixes websockets')
    visit( '/websockets' )
    page.find("#success")[:class].should == 'websockets'

    ws_url = page.find("#endpoint-session").text
    ws_url.should_not be_empty

    inbound = []

    WebSocketClient.create( ws_url ) do |client|
      client.on_message do |message|
        inbound << message
      end

      client.connect

      sleep(1)
    end

    inbound.size.should eql(1)
    inbound.first.should eql( "tacos" )
  end

  it "should be able to set attributes in the session and be visible in the subsequent web requests" do
    pending('until bob fixes websockets')
    visit( '/websockets' )
    page.find("#success")[:class].should == 'websockets'
    page.find("#food").text.should == 'tacos'

    ws_url = page.find("#endpoint-session").text
    ws_url.should_not be_empty

    inbound = []

    WebSocketClient.create( ws_url ) do |client|
      client.on_message do |message|
        inbound << message
      end

      client.connect
      sleep(1)
    end

    inbound.size.should eql(1)
    inbound.first.should eql( "tacos" )


    visit( '/websockets' )
    page.find("#food").text.should == 'beef'

  end

end

