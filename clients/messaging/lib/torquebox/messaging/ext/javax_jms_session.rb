
module javax.jms::Session

  attr_accessor :connection
  attr_accessor :naming_context

  def publish(destination_name, message)
    destination = lookup_destination( destination_name )
    producer = createProducer( destination )
    jms_message = createTextMessage( message )
    producer.send( jms_message )
    producer.close
  end

  def receive(destination_name)
    destination = lookup_destination( destination_name )
    consumer = createConsumer( destination )
    jms_message = consumer.receive
    jms_message.text
  end

  def lookup_destination(destination_name)
    @naming_context[ destination_name ]
  end

end
