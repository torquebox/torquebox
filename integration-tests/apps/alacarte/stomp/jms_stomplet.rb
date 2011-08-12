
class JmsStomplet

  include TorqueBox::Injectors

  def initialize()
    puts "initializing jms-stomplet"
    @connection_factory = inject( 'xa-connection-factory' )
    @subscriptions = {}
  end

  def xa_resources
    @xa_resources 
  end

  def configure(stomplet_config)
    puts "configuring stomplet-one #{stomplet_config.inspect}"
    @connection = @connection_factory.create_connection
    @connection.start
    @session = @connection.create_session
    @xa_resources = [ @session.xa_resource ]
  end

  def destroy
    @connection.stop
    @connection.close
  end

  def on_message(stomp_message)
    destination = @session.jms_session.create_queue( 'testQueue' )
    producer    = @session.jms_session.create_producer( destination.to_java )
    jms_message = @session.jms_session.create_text_message 
    jms_message.text = stomp_message.content_as_string
    producer.send( destination, jms_message )
  end

  def on_subscribe(subscriber)
    destination = @session.jms_session.create_queue( 'testQueue' )
    consumer = @session.jms_session.create_consumer( destination.to_java )
    consumer.message_listener = MessageListener.new( subscriber )
    @subscriptions[ subscriber ] = consumer
  end

  def on_unsubscribe(subscriber)
    subscription = @subscriptions[ subscriber ]
    subscription.close
  end


  class MessageListener
    include javax.jms.MessageListener

    def initialize(subscriber)
      @subscriber = subscriber
    end

    def onMessage(jms_message)
      stomp_message = org.projectodd.stilts.stomp::StompMessages.createStompMessage( @subscriber.destination, jms_message.text )
      @subscriber.send( stomp_message )
    end

  end

end
