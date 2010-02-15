
require 'torquebox-messaging-runtime'

Dir[ File.dirname(__FILE__) + '/../java-lib/**/*.jar' ].each do |jar|
  require jar
end

require 'torquebox/messaging/client'
