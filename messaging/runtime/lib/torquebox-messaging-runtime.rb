
require 'java'

begin
  Java::javax.jms.Connection
rescue NameError=>e
  Dir[ File.dirname(__FILE__) + '/../java-lib/**/*.jar' ].each do |jar|
    require jar
  end
end
