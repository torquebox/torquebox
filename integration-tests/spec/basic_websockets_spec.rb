require 'spec_helper'

describe "basic websockets test" do

  deploy( :application => { 
            :root => "#{File.dirname(__FILE__)}/../apps/rack/websockets", 
            :env => 'development' },
          :web => { :context => '/websockets' },
          :ruby => { :version => RUBY_VERSION[0,3] } )  

  
  it "should be deployable" do
    visit( '/websockets' )
  end

end
