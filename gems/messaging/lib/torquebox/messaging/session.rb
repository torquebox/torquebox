# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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
      
      def initialize(jms_session)
        @jms_session = jms_session
      end

      def close
        @jms_session.close
      end

      def queue_for(name)
        Queue.new( @jms_session.create_queue( name ) )
      end

      def publish(destination, payload, options={})
        producer    = @jms_session.create_producer( java_destination( destination ) )
        message     = Message.new( @jms_session, payload, options[:encoding] )

        options[:properties] ||= {}

        # This will let us create messages to be scheduled later like this:
        #
        # queue.publish(:ham => :biscuit, :scheduled => Time.now + 10)
        # queue.publish(:ham => :biscuit, :scheduled => Time.now + 2.5)
        #
        # In Rails it is possible to do:
        #
        # queue.publish(:ham => :biscuit, :scheduled => 3.minutes.from_now)
        #
        # Please note that the :scheduled parameter takes a Time object.
        if options.has_key?(:scheduled)
          options[:properties][Java::org.hornetq.api.core.Message::HDR_SCHEDULED_DELIVERY_TIME.to_s] = (options[:scheduled].to_f * 1000).to_i
        end

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
            message = decode ? Message.new( jms_message ).decode : jms_message
            block_given? ? yield(message) : message
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
        options[:timeout] ||= 10_000 # 10s
        options[:properties] ||= {}
        options[:properties]["synchronous"] = "true"
        message = publish(destination, message, options)
    
        options[:selector] = "JMSCorrelationID='#{message.jms_message.jms_message_id}'"
        receive(destination, options)
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
          request_message = Message.new( request ).decode
          options[:ttl] ||= 60_000 # 1m
          options[:encoding] ||= Message.extract_encoding_from_message( request )

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
