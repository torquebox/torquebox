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

require 'torquebox/messaging/javax_jms_text_message'

module javax.jms::Session

  attr_accessor :connection
  attr_accessor :naming_context

  def publish(destination, message, options = {})
    destination = lookup_destination( destination ) unless destination.is_a?( Java::javax.jms.Destination )
    producer = createProducer( destination )
    jms_message = create_text_message
    populate_message_headers(jms_message, options)
    populate_message_properties(jms_message, options[:properties])
    jms_message.encode message
    producer.send( jms_message,
                   options.fetch(:delivery_mode, producer.delivery_mode),
                   options.fetch(:priority, producer.priority),
                   options.fetch(:ttl, producer.time_to_live) )
    jms_message
  ensure
    producer.close unless producer.nil?
  end

  # Returns decoded message, by default.  Pass :decode=>false to
  # return the original JMS TextMessage.  Pass :timeout to give up
  # after a number of milliseconds
  def receive(destination_name, options={})
    decode = options.fetch(:decode, true)
    timeout = options.fetch(:timeout, 0)
    selector = options.fetch(:selector, nil)
    destination = lookup_destination( destination_name )
    consumer = createConsumer( destination, selector )
    jms_message = consumer.receive( timeout )
    if jms_message
      decode ? jms_message.decode : jms_message
    end
  ensure
    consumer.close unless consumer.nil?
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
      decode ? response.decode : response
    end
  end

  # Receiving end of the request-response pattern. The return value of
  # the block passed to this method is the response sent back to the
  # client. If no block is given then request is returned as the
  # response.
  def receive_and_publish(destination, options = {})
    receive_options = options.merge(:decode => false,
                                    :selector => "synchronous = 'true'")
    request = receive(destination, receive_options)
    unless request.nil?
      decoded_request = request.decode
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

  def lookup_destination(destination_name)
    @naming_context[ destination_name ]
  end

  def populate_message_headers(jms_message, options)
    return if options.nil?
    options.each do |key, value|
      case key.to_s
      when 'correlation_id' then jms_message.setJMSCorrelationID(value)
      when 'reply_to' then jms_message.setJMSReplyTo(value)
      when 'type' then jms_message.setJMSType(value)
      end
    end
  end

  def populate_message_properties(jms_message, properties)
    return if properties.nil?
    properties.each do |key, value|
      case value
      when Integer
        jms_message.set_long_property(key.to_s, value)
      when Float
        jms_message.set_double_property(key.to_s, value)
      when TrueClass, FalseClass
        jms_message.set_boolean_property(key.to_s, value)
      else
        jms_message.set_string_property(key.to_s, value.to_s)
      end
    end
  end

end

