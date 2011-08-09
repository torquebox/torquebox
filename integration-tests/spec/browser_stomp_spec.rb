require 'spec_helper'

require 'fileutils'
require 'torquebox-messaging'

describe "STOMP applications", :js=>true do

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
    visit( '/stomp-websockets/index.html' )
    page.execute_script <<-END
      connected = false;
      disconnected = false;
      client = Stomp.client( "ws://localhost:8675/" );

      client.connect( null, null, function(frame) {
        connected = true;
        client.disconnect();
        disconnected = true;
      } );
    END
    sleep( 2 )
    page_variable( :connected ).should be_true
    page_variable( :disconnected ).should be_true
  end

end

def page_variable(variable_name)
  page.evaluate_script( variable_name ) 
end
