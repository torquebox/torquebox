require 'torquebox-messaging'

class StatelessProcessor < TorqueBox::Messaging::MessageProcessor
  include TorqueBox::Injectors

  def initialize
    @already_published = false
    @queue = fetch('/queue/backchannel')
  end

  def on_message(body)
    @queue.publish('done') unless @already_published
    @already_published = true
  end
end
