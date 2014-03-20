require 'torquebox-core'

Dir.glob("#{File.dirname(__FILE__)}/wunderboss-jars/*.jar") do |jar|
  require jar
end

require 'wunderboss-rack.jar'
require 'wunderboss_rack_response_handler'

require 'torquebox/web'
