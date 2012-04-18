require 'spec_helper'
require 'torquebox-messaging'

describe "services" do
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
  
  it "should keep state after start and stop" do
    pending("JMX assertions are unpredictable in domain mode", :if => TorqueSpec.domain_mode)
    service_name = 'jboss.deployment.unit."services-knob.yml".service.SimpleService'
    verify_msc_service_state(service_name, "UP")

    # Stop the service and wait for it to go down
    set_msc_service_mode(service_name, "NEVER")
    verify_msc_service_state(service_name, "DOWN")

    # Start the service and wait for it to come up
    set_msc_service_mode(service_name, "ACTIVE")
    verify_msc_service_state(service_name, "UP")

    responseq = TorqueBox::Messaging::Queue.new( '/queue/next_response' )
    response = responseq.receive( :timeout => 120_000 )
    response.should == 'done'
  end
end
