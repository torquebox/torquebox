
include Java 

require 'weird/java-uuid-generator-3.1.3.jar'

java_import 'com.fasterxml.uuid.Generators'

require 'torquebox-messaging' 
  
class RackApp 
  def initialize 
    @log = TorqueBox::Logger.new('adapters.producer.rack_app') 
    @topic = TorqueBox::Messaging::Topic.new('/topics/producer/local') 

    # rfc-4122 GUID 
    @uuid = Generators.random_based_generator.generate
  end 
  
  def call(env) 
    request = Rack::Request.new(env) 

    h = {:adapter => 'RackApp', 
         :uuid => @uuid }

    @log.info "Sending JMS message:" 
    @topic.publish(h) 

    [200, {'Content-Type' => 'text/html'}, @uuid.to_s ]
  end 
end 
