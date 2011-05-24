require 'torquespec'
require 'capybara/dsl'
require 'akephalos'
require 'fileutils'
require 'jmx4r'

TorqueSpec.jboss_home = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jboss' ) )
TorqueSpec.max_heap = java.lang::System.getProperty( 'max.heap' )
TorqueSpec.lazy = java.lang::System.getProperty( 'jboss.lazy' ) == "true"

TorqueSpec.knob_root = File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'knobs' ) )
FileUtils.mkdir_p(TorqueSpec.knob_root) unless File.exist?(TorqueSpec.knob_root)

Capybara.register_driver :akephalos do |app|
  Capybara::Driver::Akephalos.new(app, :browser => :firefox_3)
end

Capybara.default_driver = :akephalos
Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false

RSpec.configure do |config|
  config.include Capybara
  config.after do
    Capybara.reset_sessions!
  end
end

MUTABLE_APP_BASE_PATH  = File.join( File.dirname( __FILE__ ), '..', 'target', 'apps' )
TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )

def mutable_app(path)
  full_path = File.join( MUTABLE_APP_BASE_PATH, path )
  dest_path = File.dirname( full_path )
  FileUtils.rm_rf( full_path )
  FileUtils.mkdir_p( dest_path )
  FileUtils.cp_r( File.join( File.dirname( __FILE__ ), '..', 'apps', path ), dest_path )
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
