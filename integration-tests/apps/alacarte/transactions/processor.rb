require 'torquebox-messaging'

class Processor < TorqueBox::Messaging::MessageProcessor

  include TorqueBox::Injectors

  def on_message(msg)
    output = inject( '/queue/output' )
    output.publish('yay!')
    raise msg if msg =~ /error/
  end

end

