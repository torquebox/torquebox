
module TorqueBox
  module Messaging
    module Core

      class Session

        AUTO_ACK = javax.jms::Session::AUTO_ACKNOWLEDGE
        CLIENT_ACK = javax.jms::Session::CLIENT_ACKNOWLEDGE
        DUPS_OK_ACK = javax.jms::Session::DUPS_OK_ACKNOWLEDGE

        attr_accessor :jms_session

        def initialize(jms_session)
          @jms_session = jms_session
        end

        def publish(destination, payload, options={})
          producer    = @jms_session.create_producer( destination )
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
          selector = options.fetch(:selector, nil)
          consumer = create_consumer( destination, selector )
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
          jms_message = publish(destination, wrapped_message, options)
          commit if transacted?
      
          options[:selector] = "JMSCorrelationID='#{jms_message.jms_message_id}'"
          response = receive(destination, options)
          commit if transacted?
      
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
          commit if transacted?
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
end
