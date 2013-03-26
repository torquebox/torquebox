class TermConsumer < TorqueBox::Messaging::MessageProcessor

  def initialize
    @twitter_service = TorqueBox.fetch('service:twitter-service')
  end

  def on_message(terms)
    @twitter_service.update(terms)
  end
end
