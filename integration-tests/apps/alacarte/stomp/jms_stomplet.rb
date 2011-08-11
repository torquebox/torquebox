
class JmsStomplet

  include TorqueBox::Injectors

  def initialize()
    puts "initializing jms-stomplet"
    @connection_factory = inject( 'xa-connection-factory' )
    @transaction_manager = inject( 'transaction-manager' )
  end

  def configure(stomplet_config)
    puts "configuring stomplet-one #{stomplet_config.inspect}"
    @connection = @connection_factory.create_connection
  end

  def destroy
    @connection.close
  end

  def on_message(message)
    puts "receive message #{message} with #{@connection_factory}"
    session = @connection.create_session
    transaction = @transaction_manager.transaction

    if ( transaction ) 
      transaction.enlist_resource( session.xa_resource )
    end

    session.publish( session.queue_for( 'testQueue' ), message.content_as_string )

    if ( transaction )
      transaction.delist_resource( session.xa_resource, javax.transaction.xa::XAResource::TMSUSPEND )
    end
  end

  def on_subscribe(subscriber)
    puts "subscribe #{subscriber} with #{@connection_factory}"
  end

  def on_unsubscribe(subscriber)
    puts "unsubscribe to stomplet-one: #{subscriber}"
  end



end
