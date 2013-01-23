class TermConsumer < TorqueBox::Messaging::MessageProcessor
  include TorqueBox::Injectors

  def initialize
    @twitter_service = fetch('service:twitter-service')
  end

  def on_message(terms)
    @twitter_service.update(terms)
  end
end
