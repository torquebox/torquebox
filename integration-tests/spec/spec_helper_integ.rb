##
## This file executes only on the client side, and
## is not shipped to the server for execution, ever.
##

require 'ostruct'
require 'capybara/dsl'
require 'jmx4r'
require 'websocket_client'
require 'stilts-stomp-client'

driver_type = java.lang::System.getProperty( "driver.type" ) || 'headless'

if ( driver_type == 'browser' )
  class Cookies 
    def initialize(manage)
      @manage = manage
    end
    
    def clear
      @manage.delete_all_cookies()
    end

    def count
      @manage.all_cookies.size
    end

    def [](name)
      @manage.all_cookies.each do |cookie|
        return OpenStruct.new( cookie ) if ( cookie[:name] == name )
      end
      nil
    end
  end

  puts "using browser mode"
  require "selenium-webdriver"
  Capybara.register_driver :browser do |app|
    require 'selenium/webdriver'
    profile = Selenium::WebDriver::Firefox::Profile.new
    #profile['general.useragent.override'] = "iPhone"
   
    driver = Capybara::Driver::Selenium.new(app, :profile => profile)
    def driver.cookies
      @cookies ||= Cookies.new( browser.manage )
    end
    driver
  end

  class Selenium::WebDriver::Element
    def value
      attribute( 'value' )
    end
  end

  Capybara.default_driver = :browser
else
  puts "using headless mode"
  require 'akephalos'
  Capybara.register_driver :akephalos do |app|
    Capybara::Driver::Akephalos.new(app, :browser => :firefox_3)
  end

  Capybara.default_driver = :akephalos
end

Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false

RSpec.configure do |config|
  config.include Capybara
  config.after do
    Capybara.reset_sessions!
  end
end

def add_request_header(key, value)
  page.driver.browser.send(:client).add_request_header(key, value)
end

def mbean(name)
  JMX::MBean.establish_connection :command => /org.jboss.as.standalone/i
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
