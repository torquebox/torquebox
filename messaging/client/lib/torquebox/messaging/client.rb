require 'base64'

class Java::org.torquebox.messaging.client::Client

  alias_method :core_send, :send

  def send(destination, opts)
    message = nil
    if ( ( ! opts[:task].nil? ) && ( ! opts[:payload].nil? ) )
      puts "sending a TASK #{opts.inspect}"
      message = session.create_text_message
      message.set_string_property( 'torqueboxMessageType', 'task' )
      message.set_string_property( 'torqueboxTask', opts[:task] )
      marshalled = Marshal.dump( opts[:payload] )
      encoded = Base64.encode64( marshalled )
      message.text = encoded
    elsif ( ! opts[:object].nil? )
      message = session.create_text_message
      message.set_string_property( 'torqueboxMessageType', 'object' )
      marshalled = Marshal.dump( opts[:object] )
      encoded = Base64.encode64( marshalled )
      message.text = encoded
    elsif ( ! opts[:text].nil? )
      message = session.create_text_message
      message.set_string_property( 'torqueboxMessageType', 'text' )
      message.text = opts[:text].to_s 
    end
    if ( message.nil? )
      puts "no message to send for #{opts.inspect}, :text or :object required"
    else
      producer = session.create_producer( self.lookup( destination ) )
      producer.send( message )
    end
  end

end

module TorqueBox
  module Messaging

    class Client 
      def self.connect(opts={},&block)
        factory = Java::org.torquebox.messaging.client::ClientFactory.new
        factory.naming_provider_url          = opts[:naming_provider_url]          unless ( opts[:naming_provider_url].nil? )
        factory.context_factory_class_name   = opts[:context_factory_class_name]   unless ( opts[:context_factory_class_name].nil? )
        factory.url_package_prefixes         = opts[:url_package_prefixes]         unless ( opts[:url_package_prefixes].nil? )
        factory.connection_factory_jndi_name = opts[:connection_factory_jndi_name] unless ( opts[:connection_factory_jndi_name].nil? )
        client = factory.create_client
        client.connect
        if ( block )
          begin
            block.call( client )
            client.commit
          rescue => e
            client.rollback
            raise e
          ensure
            client.close
          end
        else
          return client
        end
      end

    end

  end
end
