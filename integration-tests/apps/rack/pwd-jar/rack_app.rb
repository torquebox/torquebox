
include Java 

require 'weird/uuid-3.2.jar' 

java_import 'com.eaio.uuid.UUID' 

require 'torquebox-messaging' 
  
class RackApp 
  def initialize 
    @log = TorqueBox::Logger.new('adapters.producer.rack_app') 
    @topic = TorqueBox::Messaging::Topic.new('/topics/producer/local') 

    # rfc-4122 GUID 
    @uuid = UUID.new 
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
