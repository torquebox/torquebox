
class StompletOne

  def initialize()
    puts "initializing stomplet-one"
    @subscribers = {}
  end

  def configure(stomplet_config)
    puts "configuring stomplet-one #{stomplet_config.inspect}"
  end

  def on_message(message, session)
    @subscribers.values.each do |sink|
      sink.send( message )
    end 
  end

  def on_subscribe(subscriber)
    puts "unsubscribe to stomplet-one: #{subscriber}"
    @subscribers[ subscriber.getId() ] = subscriber
  end

  def on_unsubscribe(subscriber)
    puts "unsubscribe to stomplet-one: #{subscriber}"
    @subscribers.delete( subscriber.getId() )
  end



end
