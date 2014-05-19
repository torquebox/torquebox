require 'torquebox-core'

Dir.glob("#{File.dirname(__FILE__)}/wunderboss-jars/*.jar") do |jar|
  TorqueBox::Jars.register_and_require(jar)
end

require 'torquebox/scheduling'
