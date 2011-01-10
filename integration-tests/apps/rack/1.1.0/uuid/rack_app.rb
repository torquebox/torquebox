
include Java 

#require 'rubygems' 
#require 'lib/log4j.jar' 
require 'lib/uuid-3.2.jar' 

java_import 'com.eaio.uuid.UUID' 

require 'org.torquebox.torquebox-messaging-client' 
  
class RackApp 
  def initialize 
    @log = org.apache.log4j.Logger.getLogger('adapters.producer.rack_app') 
    @topic = TorqueBox::Messaging::Topic.new('/topics/producer/local') 

    # rfc-4122 GUID 
    @uuid = UUID.new # throws exception 
  end 
  
  def call(env) 
    request = Rack::Request.new(env) 
   

    h = {:adapter => 'RackApp', 
         :uuid => '6ffb022a-214a-4084-a929-3bd1ae730e04'} 

    @log.info "Sending JMS message:" 
    @topic.publish(h) 

    [200, {'Content-Type' => 'text/plain'}, "published JMS event"] 
  end 
end 
