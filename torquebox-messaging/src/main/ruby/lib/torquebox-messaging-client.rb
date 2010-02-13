
Dir[ File.dirname(__FILE__) + '/../java-lib/**/*.jar' ].each do |jar|
  require jar
end

require 'torquebox/messaging/client'
