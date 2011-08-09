

require 'ostruct'
require 'capybara/dsl'
require "selenium-webdriver"
require 'akephalos'

class BrowserCookies 
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

# Monkey patches against the Selenium WebDriver to act more like the akephalos driver
class Capybara::Driver::Selenium
  def cookies
    @cookies ||= BrowserCookies.new( browser.manage );
  end
end

class Selenium::WebDriver::Element
  def value
    attribute( 'value' )
  end
end

# Determine and setup default driver

driver_type = java.lang::System.getProperty( "driver.type" ) || 'any'

def register_headless_driver()
  Capybara.register_driver :headless do |app|
    Capybara::Driver::Akephalos.new(app, :browser => :firefox_3, :resynchronize=>false)
  end
end

def register_browser_driver()
  Capybara.register_driver :browser do |app|
    profile = Selenium::WebDriver::Firefox::Profile.new
    profile['network.websocket.override-security-block'] = true
    Capybara::Driver::Selenium.new(app, :profile => profile)
  end
end

case ( driver_type )
  when 'headless'
    register_headless_driver
    Capybara.default_driver = :headless
    DRIVER_MODE=:headless
  when 'browser'
    register_browser_driver
    Capybara.default_driver = :browser
    Capybara.javascript_driver = :browser
    DRIVER_MODE=:browser
  else
    register_headless_driver
    register_browser_driver
    Capybara.default_driver = :headless
    Capybara.javascript_driver = :browser
    DRIVER_MODE=:any
end

Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false

RSpec.configure do |config|
  config.include Capybara
  config.exclusion_filter = { 
    :js=>lambda{|val| 
      ( val && ( DRIVER_MODE == :headless ) )
    },
    :browser_not_supported=>lambda{|val|
      ( val && ( DRIVER_MODE == :browser ) )
    },
   
  }
  config.after do
    Capybara.reset_sessions!
    Capybara.use_default_driver
  end
  config.before do
    Capybara.current_driver = Capybara.javascript_driver if example.metadata[:js]
    Capybara.current_driver = example.metadata[:driver] if example.metadata[:driver]
  end
end

def add_request_header(key, value)
  page.driver.browser.send(:client).add_request_header(key, value)
end
