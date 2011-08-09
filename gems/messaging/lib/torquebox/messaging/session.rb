# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

module TorqueBox
  module Messaging

    class Session

      AUTO_ACK = javax.jms::Session::AUTO_ACKNOWLEDGE
      CLIENT_ACK = javax.jms::Session::CLIENT_ACKNOWLEDGE
      DUPS_OK_ACK = javax.jms::Session::DUPS_OK_ACKNOWLEDGE

      attr_accessor :jms_session
      attr_accessor :connection
      
      def initialize(jms_session, connection)
        @jms_session = jms_session
        @connection = connection
      end

      def transacted?
        @jms_session.transacted?
      end

      def close
        @jms_session.close
      end

      def commit
        @jms_session.commit
      end

      def queue_for(name)
        Queue.new( @jms_session.create_queue( name ) )
      end

      def publish(destination, payload, options={})
        producer    = @jms_session.create_producer( java_destination( destination ) )
        message     = Message.new( @jms_session.create_text_message, payload )

        message.populate_message_headers(options)
        message.populate_message_properties(options[:properties])

        producer.send( message.jms_message,
                       options.fetch(:delivery_mode, producer.delivery_mode),
                       options.fetch(:priority, producer.priority),
                       options.fetch(:ttl, producer.time_to_live) )
        message
      end

      # Returns decoded message, by default.  Pass :decode=>false to
      # return the original JMS TextMessage.  Pass :timeout to give up
      # after a number of milliseconds
      def receive(destination, options={})
        decode = options.fetch(:decode, true)
        timeout = options.fetch(:timeout, 0)
        selector = options[:selector]
        
        java_destination = java_destination( destination )
        if options[:durable] && java_destination.class.name =~ /Topic/
      raise ArgumentError.new( "You must set the :client_id via Topic's connect_options to use :durable" ) unless connection.client_id
          consumer = @jms_session.createDurableSubscriber( java_destination,
                                                           options.fetch(:subscriber_name, Topic::DEFAULT_SUBSCRIBER_NAME),
                                                           selector,
                                                           false )
        else
          consumer = @jms_session.createConsumer( java_destination, selector )      
        end
        begin
          jms_message = consumer.receive( timeout )
          if jms_message
            decode ? Message.decode( jms_message ) : jms_message
          end
        ensure
          consumer.close unless consumer.nil?
        end
      end

      # Implement the request-response pattern. Sends a message to the
      # request destination and waits for a reply on the response
      # destination.
      #
      # Options:
      #
      # :timeout - specifies the time in miliseconds to wait for answer,
      #            default: 10000 (10s)
      # :decode  - pass false to return the original JMS TextMessage,
      #            default: true
      #
      def publish_and_receive(destination, message, options = {})
        options[:timeout] ||= 10000 # 10s
        decode = options.fetch(:decode, false)
        options[:properties] ||= {}
        options[:properties]["synchronous"] = "true"
        wrapped_message = { :timeout => options[:timeout], :message => message }
        message = publish(destination, wrapped_message, options)
    
        options[:selector] = "JMSCorrelationID='#{message.jms_message.jms_message_id}'"
        response = receive(destination, options)
    
        if response
          decode ? Message.decode( response ): response
        end
      end

      # Receiving end of the request-response pattern. The return value of
      # the block passed to this method is the response sent back to the
      # client. If no block is given then request is returned as the
      # response.
      def receive_and_publish(destination, options = {})
        selector = "synchronous = 'true'"
        selector = "#{selector} and (#{options[:selector]})" if options[:selector]
        receive_options = options.merge(:decode => false,
                                        :selector => selector)
    
        request = receive(destination, receive_options)
        unless request.nil?
          decoded_request = Message.decode( request )
          request_message = decoded_request[:message]
          # Base the response ttl off the original request timeout
          request_timeout = decoded_request[:timeout]
          options[:ttl] ||= request_timeout
    
          response = block_given? ? yield(request_message) : request_message
    
          options[:correlation_id] = request.jms_message_id
          publish(destination, response, options)
        end
      end

      def unsubscribe(subscriber_name = Topic::DEFAULT_SUBSCRIBER_NAME)
        @jms_session.unsubscribe( subscriber_name )
      end
      
      def create_browser(*args)
        jms_session.create_browser( *args )
      end
      
      def java_destination(destination)
        java_destination = destination.name
        
        unless java_destination.is_a?( javax.jms.Destination )
          meth = destination.is_a?( Queue ) ? :create_queue : :create_topic
          java_destination = @jms_session.send( meth, java_destination )
        end
        
        java_destination
      end
      
      def self.canonical_ack_mode(ack_mode)
        case ( ack_mode )
          when Fixnum
            return ack_mode
          when :auto
            return AUTO_ACK
          when :client
            return CLIENT_ACK
          when :dups_ok
            return DUPS_OK_ACK
        end
      end
    end

  end
end
