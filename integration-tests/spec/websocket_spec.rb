require 'spec_helper'

describe "websocket support" do

  deploy( :application => { 
            :root => "#{File.dirname(__FILE__)}/../apps/rails3/websocket_demo", 
            :env => 'development' },
          :web => { :context => '/basic-rack' },
          :ruby => { :version => RUBY_VERSION[0,3] } )  

  it "should work" do
    visit "/websockets"
  end

end

