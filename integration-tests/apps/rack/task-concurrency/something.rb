require 'torquebox-messaging'

class Something
  include TorqueBox::Messaging::Backgroundable

  always_background :foo

  def initialize
    @backchannel = TorqueBox.fetch("/queues/backchannel")
  end

  def foo
    puts "FOO in Something #{Thread.current.object_id}"
    @backchannel.publish Thread.current.object_id
    nil
  end
end
