##
## This file executes only on the client side, and
## is not shipped to the server for execution, ever.
##

require 'ostruct'
require 'capybara/dsl'
require 'jmx4r'
require 'stilts-stomp-client'

require 'driver_helper'

def mbean(name)
  retries = 0
  begin
    JMX::MBean.establish_connection :url => 'service:jmx:remoting-jmx://127.0.0.1:9999'
  rescue java.lang.RuntimeException => ex
    retries += 1
    if retries < 5
      sleep 0.5
      retry
    else
      raise ex
    end
  end
  yield JMX::MBean.find_by_name(name)
ensure
  JMX::MBean.remove_connection
end

def get_msc_service_state(service_name)
  mbean('jboss.msc:type=container,name=jboss-as') do |msc|
    msc.getServiceStatus(service_name).get('stateName')
  end
end

def set_msc_service_mode(service_name, mode)
  mbean('jboss.msc:type=container,name=jboss-as') do |msc|
    msc.setServiceMode(service_name, mode)
  end
end

def verify_msc_service_state(service_name, state, options={})
  options[:timeout] ||= 30 # default to wait 30 seconds
  service_state = nil
  elapsed_seconds = 0
  until service_state == state || elapsed_seconds > options[:timeout] do
    service_state = get_msc_service_state(service_name)
    sleep(0.5)
  end
  service_state.should == state
end
