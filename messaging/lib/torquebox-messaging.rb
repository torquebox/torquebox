require 'torquebox-core'

Dir.glob("#{File.dirname(__FILE__)}/wunderboss-jars/*.jar") do |jar|
  require jar
end

require 'torquebox/messaging'
