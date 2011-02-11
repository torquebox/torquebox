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
    jms_message.encode message
    producer.send( jms_message,
                   options.fetch(:delivery_mode, producer.delivery_mode),
                   options.fetch(:priority, producer.priority),
                   options.fetch(:ttl, producer.time_to_live) )
    producer.close
  end

  # Returns decoded message, by default.  Pass :decode=>false to
  # return the original JMS TextMessage.  Pass :timeout to give up
  # after a number of milliseconds
  def receive(destination_name, options={})
    decode = options.fetch(:decode, true)
    timeout = options.fetch(:timeout, 0)
    destination = lookup_destination( destination_name )
    consumer = createConsumer( destination )
    jms_message = consumer.receive( timeout )
    if jms_message
      decode ? jms_message.decode : jms_message
    end
  end

  # Sends a message to specified destination, creates a temporary
  # queue and waits for reply (request-reply pattern).
  #
  # Options:
  #
  # :timeout - specifies the time in miliseconds to wait for answer,
  #            default: 10000 (10s)
  # :decode  - pass false to return the original JMS TextMessage,
  #            default: true
  #
  def send_and_receive(destination_name, message, options = {})
    decode = options.fetch(:decode, true)
    timeout = options.fetch(:timeout, 10000) # 10s
    destination = lookup_destination( destination_name )

    request_producer = createProducer( destination )

    reply_queue = createTemporaryQueue
    reply_receiver = createConsumer( reply_queue )

    jms_message = createTextMessage
    jms_message.jmsreply_to = reply_queue
    jms_message.jmsdelivery_mode = Java::javax.jms.DeliveryMode.NON_PERSISTENT
    jms_message.encode message

    request_producer.send( jms_message )
    commit
    jms_message = reply_receiver.receive( timeout )
    commit

    if jms_message
      decode ? jms_message.decode : jms_message
    end
  end

  def lookup_destination(destination_name)
    @naming_context[ destination_name ]
  end

end

