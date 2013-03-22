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

  it 'should work for valid injections' do
    TorqueBox.fetch('service:SimpleService').should_not be_nil
    TorqueBox.fetch('/queue/container_queue').should_not be_nil
  end

  it 'should raise InjectionError for invalid injection' do
    lambda {
      TorqueBox.fetch('something_not_valid')
    }.should raise_error(TorqueBox::InjectionError)
  end

end
