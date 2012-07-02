require 'torquebox-messaging'


class SimpleProcessor < TorqueBox::Messaging::MessageProcessor

  include TorqueBox::Injectors

  def initialize
    @cache = TorqueBox::Infinispan::Cache.new(:name=>'processor_cache')
  end

  def on_message(body)
    if ( body[:action] == "write" )
      @cache.put( 'simple_processor_key', body[:message] )
    else
      queue = fetch( '/queue/backchannel' )
      queue.publish( @cache.get('simple_processor_key') )
    end
  end

end
