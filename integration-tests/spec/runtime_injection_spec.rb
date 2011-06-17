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
    analyzer = inject( 'runtime-injection-analyzer' ) 
    analyzer.should_not be_nil
  end

  it "should be able to analyzer at runtime" do
    analyzer = inject( 'runtime-injection-analyzer' ) 
    analyzer.should_not be_nil
    inject( 'service:SimpleService' ).should be_nil
    analyzer.analyze_and_inject do 
      inject( 'service:SimpleService' )
    end
    inject( 'service:SimpleService' ).should_not be_nil
  end


end
