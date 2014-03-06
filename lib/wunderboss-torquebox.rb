Dir.glob("#{File.dirname(__FILE__)}/wunderboss-jars/*.jar") do |jar|
  require jar
end

require 'wunderboss-torquebox.jar'
require 'wunderboss-torquebox/response_handler'
