class Thing < ActiveRecord::Base

  attr_reader :callback
  attr_accessor :publish_callback
  alias_method :publish_callback?, :publish_callback

  after_commit   { report( 'after_commit' ) }
  after_rollback { report( 'after_rollback' ) }

  def report(callback)
    if publish_callback?
      TorqueBox::Messaging::Queue.new('/queue/output').publish(callback, :tx => false)
    end
    @callback = callback
  end

end
