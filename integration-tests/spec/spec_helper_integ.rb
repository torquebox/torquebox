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
  url = JMX::JDKHelper.find_local_url(/org.jboss.as.standalone/i)
  puts "default jmx url: " + url
  url.gsub!(/\d+\.\d+\.\d+\.\d+/, "127.0.0.1")
  puts "using jmx url: " + url
  JMX::MBean.establish_connection :url => url #:command => /org.jboss.as.standalone/i
  JMX::MBean.find_by_name(name)
end

def get_msc_service_state(service_name)
  @msc ||= mbean('jboss.msc:type=container,name=jboss-as')
  @msc.getServiceStatus(service_name).get('stateName')
end

def set_msc_service_mode(service_name, mode)
  @msc ||= mbean('jboss.msc:type=container,name=jboss-as')
  @msc.setServiceMode(service_name, mode)
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
