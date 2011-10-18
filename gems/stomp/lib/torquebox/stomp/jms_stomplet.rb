module TorqueBox
  module Stomp
    class JmsStomplet

      include TorqueBox::Injectors
    
      def initialize()
        @connection_factory = inject( 'xa-connection-factory' )
        @subscriptions = {}
      end
    
      def xa_resources
        @xa_resources 
      end
    
      def configure(stomplet_config)
        @connection = @connection_factory.create_connection
        @connection.start
        @session = @connection.create_session
        @xa_resources = [ @session.xa_resource ]
      end
    
      def destroy
        @session.close
        @connection.close
      end

      # -----
      # -----
    
      def on_unsubscribe(subscriber)
        subscriptions = @subscriptions.delete( subscriber )
        subscriptions.each do |subscription|
          subscription.close
        end
      end
    
      # -----
      # -----

      def queue(name)
        jms_session = @session.jms_session
        TorqueBox::Messaging::Queue.new( jms_session.create_queue( name ) )
      end

      def topic(name)
        jms_session = @session.jms_session
        TorqueBox::Messaging::Topic.new( jms_session.create_topic( name ) )
      end

      def destination_for(name, type)
        return queue(name) if ( type.to_sym == :queue )
        topic(name)
      end
    
      def subscribe_to(subscriber, destination, selector=nil)
        jms_session = @session.jms_session
        java_destination = @session.java_destination( destination )
        consumer = @session.jms_session.create_consumer( java_destination.to_java, selector )
        consumer.message_listener = MessageListener.new( subscriber )
        @subscriptions[ subscriber ] ||= []
        @subscriptions[ subscriber ] << consumer
      end
    
      def send_to(destination, stomp_message, headers={})
        jms_session = @session.jms_session
        java_destination = @session.java_destination( destination )
    
        producer    = @session.jms_session.create_producer( java_destination.to_java )

        case ( stomp_message ) 
        when org.projectodd.stilts.stomp::StompMessage
          content = stomp_message.content_as_string
        else
          content = jms_message
        end

        encoded_message = TorqueBox::Messaging::Message.new( @session.jms_session, content )
        jms_message = encoded_message.jms_message

        if ( stomp_message.is_a?( org.projectodd.stilts.stomp::StompMessage ) )
          stomp_message.headers.header_names.each do |name|
            jms_name = name.to_s.gsub( /-/, '_' )
            header_value = stomp_message.headers[ name.to_s ]
            jms_message.setStringProperty( jms_name, header_value )
          end
        end

        headers.each do |name, header_value|
          jms_name = name.to_s.gsub( /-/, '_' )
          jms_message.setStringProperty( jms_name, header_value.to_s )
        end

        producer.send( jms_message )
      end
    
      class MessageListener
        include javax.jms.MessageListener
    
        def initialize(subscriber)
          @subscriber = subscriber
        end
    
        def onMessage(jms_message)
          stomp_message = TorqueBox::Stomp::Message.new( TorqueBox::Messaging::Message.new( jms_message ).decode )
          jms_message.property_names.each do |name|
            value = jms_message.getObjectProperty( name ).to_s
            stomp_message.headers.put( name.to_s.to_java( java.lang.String ), value.to_java( java.lang.String) ) if value
          end
          @subscriber.send( stomp_message )
        end
    
      end
    
    end

  end
end 
