require 'spec_helper'

remote_describe 'runtime injection' do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/services
      env: development
    
    environment:
      BASEDIR: #{File.dirname(__FILE__)}/..
    
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  require 'torquebox-core'

  include TorqueBox::Injectors

  it "should be able to grab the runtime analyzer" do
    #pending("need to figure out why this occasionally fails weirdly")
    analyzer = fetch( 'runtime-injection-analyzer' )
    analyzer.should_not be_nil
  end

  it "should be able to perform analysis at runtime" do
    analyzer = fetch( 'runtime-injection-analyzer' )
    analyzer.should_not be_nil
    fetch( 'service:SimpleService' ).should_not be_nil
    fetch( '/queue/container_queue' ).should_not be_nil
  end

end
