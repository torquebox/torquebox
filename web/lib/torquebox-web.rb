require 'torquebox-core'

Dir.glob("#{File.dirname(__FILE__)}/wunderboss-jars/*.jar") do |jar|
  TorqueBox::Jars.register_and_require(jar)
end

TorqueBox::Jars.register_and_require("#{File.dirname(__FILE__)}/wunderboss-rack.jar")
require 'wunderboss_rack_response_handler'

require 'torquebox/web'
