# -*- coding: utf-8 -*-
require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "STOMP applications via websockets", :js=>true do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/stomp
      env: development
    
    web:
      context: /stomp-websockets
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it "should be able to connect and disconnect using stomp over websockets" do
    visit( '/stomp-websockets/give-me-a-cookie-please' )
    visit( '/stomp-websockets/index.html' )
    page.execute_script <<-END
      connected = false;
      disconnected = false;
      complete = null;
      client = new Stomp.Client();

      client.connect( function(frame) {
        connected = true;
        client.disconnect();
        disconnected = true;
        complete = true;
      } );
    END
    wait_for( :complete ).should_not be_nil
    page_variable( :connected ).should be_true
    page_variable( :disconnected ).should be_true
  end

  it "should be able to subscribe send and receive" do
    visit( '/stomp-websockets/give-me-a-cookie-please' )
    visit( '/stomp-websockets/index.html' )
    page.execute_script <<-END
      received_message = null;
      subscribed       = null;
      disconnected     = null;

      client = new Stomp.Client();

      client.connect( function(frame) {
        client.subscribe( "/queues/foo", function(message){
          received_message = message;
          client.disconnect();
          disconnected = true;
        } );
        subscribed = true;
      } );
    END

    wait_for( :subscribed ).should_not be_nil

    sleep( 1 )

    page.execute_script <<-END
      client.send( "/queues/foo", {}, "this is my message" )
    END

    wait_for( :disconnected ).should_not be_nil
    message = page_variable( :received_message )
    page_variable( :received_message )['body'].should == "this is my message"
  end


  it "should be able to subscribe send and receive from JMS, transactionally" do
    visit( '/stomp-websockets/give-me-a-cookie-please' )
    visit( '/stomp-websockets/index.html' )
    page.execute_script <<-END
      received_message = null;
      subscribed       = null;
      disconnected     = null;

      client = new Stomp.Client();

      client.connect( function(frame) {
        client.subscribe( "/bridge/foo", function(message){
          received_message = message;
          disconnected = true;
        } );
        subscribed = true;
      } );
    END

    wait_for( :subscribed ).should_not be_nil

    sleep( 3 )

    page.execute_script <<-END
      client.begin( 'tx-1' );
      client.send( "/bridge/foo", { transaction: 'tx-1' }, "this is my message" );
    END

    sleep( 3 )

    page_variable( :received_message ).should be_nil

    page.execute_script <<-END
      client.commit( 'tx-1' );
    END

    sleep( 3 )

    page.execute_script <<-END
      client.disconnect();
    END

    wait_for( :disconnected ).should_not be_nil
    message = page_variable( :received_message )
    message['body'].should == "this is my message"
  end

  it "should be able to subscribe send and receive utf8 messages" do
    visit( '/stomp-websockets/give-me-a-cookie-please' )
    visit( '/stomp-websockets/index.html' )
    page.execute_script <<-END
      received_message = null;
      subscribed       = null;
      disconnected     = null;

      client = new Stomp.Client();

      client.connect( function(frame) {
        client.subscribe( "/queues/foo", function(message){
          received_message = message;
          client.disconnect();
          disconnected = true;
        } );
        subscribed = true;
      } );
    END

    wait_for( :subscribed ).should_not be_nil

    sleep( 1 )

    page.execute_script <<-END
      client.send( "/queues/foo", {}, "ääbb" )
    END

    wait_for( :disconnected ).should_not be_nil
    body = page_variable( :received_message )['body']
    # We have to force encoding to UTF-8 because for some reason it
    # comes though as ASCII-8BIT when passed from Firefox via Capybara
    body.force_encoding('UTF-8') if body.respond_to?(:force_encoding)
    body.should == "ääbb"
  end

end

def page_variable(variable_name)
  page.evaluate_script( variable_name ) 
end

def wait_for(variable_name, timeout_seconds=30)
  0.upto( timeout_seconds ) do
    v = page_variable( variable_name )
    return v unless ( v.nil? )
    sleep( 1 )
  end 
  nil
end
