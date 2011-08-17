
require 'torquebox-stomp'

class BridgeStomplet < TorqueBox::Stomp::JmsStomplet

  def initialize()
    super
  end

  def configure(stomplet_config)
    super
   
    @destination_type = stomplet_config['type']
    @destination_name = stomplet_config['destination']
  end

  def on_message(stomp_message, session)
    puts "session is #{session}"
    puts "food is #{session[:food]}"
    send_to( stomp_message, @destination_name, @destination_type )
  end

  def on_subscribe(subscriber)
    subscribe_to( subscriber, @destination_name, @destination_type )
  end

end
