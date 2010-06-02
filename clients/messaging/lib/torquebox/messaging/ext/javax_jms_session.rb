
module javax.jms::Session

  attr_accessor :connection
  attr_accessor :naming_context

  def publish(destination_name, message)
    destination = lookup_destination( destination_name )
    producer = createProducer( destination )
    producer.time_to_live = 5000
    ttl = producer.time_to_live
    puts "PUBLISH original #{message}"
    jms_message = createTextMessage( message )
    puts "PUBLISH jms_message #{jms_message} with TTL #{ttl}"
    producer.send( jms_message )
    producer.close
  end

  def receive(destination_name)
    destination = lookup_destination( destination_name )
    consumer = createConsumer( destination )
    puts "CONSUME #{consumer}"
    jms_message = consumer.receive
    puts "CONSUME received #{jms_message}"
    jms_message.text
  end

  def lookup_destination(destination_name)
    @naming_context[ destination_name ]
  end

end
